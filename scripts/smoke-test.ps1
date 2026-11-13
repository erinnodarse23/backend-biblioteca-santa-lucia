param(
    [switch]$SkipDbReset,
    [switch]$SkipBuild,
    [string]$DbName = "biblioteca_test"
)

$ErrorActionPreference = 'Continue'
Set-Location (Split-Path $PSScriptRoot -Parent)

function Title($m) { Write-Host ""; Write-Host "=== $m ===" -ForegroundColor Magenta }
function Ok($m)    { Write-Host "  [OK]   $m" -ForegroundColor Green; $script:pass++ }
function Fail($m)  { Write-Host "  [FAIL] $m" -ForegroundColor Red; $script:fail++ }
function Info($m)  { Write-Host "  [INFO] $m" -ForegroundColor DarkGray }
function Sect($m)  { Write-Host ""; Write-Host "> $m" -ForegroundColor Cyan }

$script:pass = 0
$script:fail = 0

function AssertEq($actual, $expected, $label) {
    if ($actual -eq $expected) { Ok "$label -> $actual" }
    else { Fail "$label -> esperado $expected, obtenido $actual" }
}

Title "FASE 0 - Diagnostico"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) { Write-Host "java no en PATH"; exit 1 }
if (-not (Get-Command mvn  -ErrorAction SilentlyContinue)) { Write-Host "mvn no en PATH";  exit 1 }
if (-not (Get-Command psql -ErrorAction SilentlyContinue)) { Write-Host "psql no en PATH"; exit 1 }
Ok "java, mvn, psql disponibles"

if (-not $env:DB_PASSWORD) {
    $sec = Read-Host "Password de postgres" -AsSecureString
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))
}
$env:PGPASSWORD = $env:DB_PASSWORD
Ok "DB_PASSWORD definido (len=$($env:DB_PASSWORD.Length))"

$inUse = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($inUse) {
    Fail "Puerto 8080 ocupado por PID $($inUse.OwningProcess). Cierralo (ej: taskkill /PID $($inUse.OwningProcess) /F)"
    exit 1
}
Ok "Puerto 8080 libre"

Title "FASE 1 - BD $DbName"

if (-not $SkipDbReset) {
    & psql -U postgres -c "DROP DATABASE IF EXISTS $DbName;" 2>&1 | Out-Null
    & psql -U postgres -c "CREATE DATABASE $DbName;" 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) { Fail "No pude crear la BD"; exit 1 }
    Ok "BD creada"

    & psql -U postgres -d $DbName -f "scripts/tablas.sql" 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) { Fail "tablas.sql fallo"; exit 1 }
    Ok "tablas.sql aplicado"

    $indexes = & psql -U postgres -d $DbName -t -A -c "SELECT indexname FROM pg_indexes WHERE indexname LIKE 'idx_%' ORDER BY indexname;" 2>&1
    $expected = @('idx_libro_autor','idx_libro_genero','idx_prestamo_activo','idx_prestamo_isbn','idx_prestamo_usuario','idx_resena_isbn','idx_resena_usuario')
    foreach ($i in $expected) {
        if ($indexes -contains $i) { Ok "indice $i" } else { Fail "falta indice $i" }
    }

    $seed = "INSERT INTO autor (nombre, apellido, nacionalidad) VALUES ('Gabriel','Garcia Marquez','Colombiana') RETURNING id_autor; " +
            "INSERT INTO genero (nombre, descripcion) VALUES ('Realismo magico','Genero literario') RETURNING id_genero; " +
            "INSERT INTO usuario (nombre, apellido, email, direccion) VALUES ('Ana','Perez','ana@test.cu','Calle 1') RETURNING id_usuario;"
    $seed | & psql -U postgres -d $DbName 2>&1 | Out-Null
    Ok "seed insertado"
} else {
    Info "SkipDbReset activo"
}

Title "FASE 2 - Compilar y arrancar"

if (-not $SkipBuild) {
    & mvn -q package -DskipTests
    if ($LASTEXITCODE -ne 0) { Fail "mvn package fallo"; exit 1 }
    Ok "jar generado"
}

$jar = Get-ChildItem "target\spring-biblioteca-*.jar" | Where-Object { $_.Name -notmatch 'original' } | Select-Object -First 1
if (-not $jar) { Fail "no hay jar en target/"; exit 1 }
Info "jar: $($jar.Name)"

$logOut = "target\smoke-server.log"
$logErr = "target\smoke-server.err"
Remove-Item $logOut, $logErr -ErrorAction SilentlyContinue

$argStr = "-jar `"$($jar.FullName)`" --spring.datasource.url=jdbc:postgresql://localhost:5432/$DbName --spring.datasource.password=$env:DB_PASSWORD"
$server = Start-Process -FilePath "java" -ArgumentList $argStr -RedirectStandardOutput $logOut -RedirectStandardError $logErr -PassThru -NoNewWindow
Ok "servidor PID $($server.Id). log: $logOut"

$ready = $false
$elapsed = 0
while (-not $ready -and $elapsed -lt 90) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/api/libros" -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop | Out-Null
        $ready = $true
    } catch {
        if ($server.HasExited) {
            Fail "servidor murio. Ultimas lineas:"
            Get-Content $logErr -Tail 30 | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkRed }
            exit 1
        }
    }
}
if ($ready) { Ok "servidor listo en ${elapsed}s" }
else {
    Fail "servidor no respondio en 90s"
    Get-Content $logErr -Tail 20
    Stop-Process -Id $server.Id -Force
    exit 1
}

function InvokeApi {
    param([string]$Method, [string]$Path, $Body = $null)
    $params = @{
        Method = $Method
        Uri = "http://localhost:8080$Path"
        TimeoutSec = 10
        UseBasicParsing = $true
        ErrorAction = 'Stop'
    }
    if ($Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 5 -Compress)
        $params.ContentType = 'application/json'
    }
    try {
        $resp = Invoke-WebRequest @params
        return @{ Status = [int]$resp.StatusCode; Body = $resp.Content }
    } catch {
        $status = 0
        $body = ''
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try { $body = (New-Object IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd() } catch {}
        }
        return @{ Status = $status; Body = $body }
    }
}

Title "FASE 3 - Smoke tests"

Sect "1) Swagger UI (regresion springdoc 2.5.0)"
AssertEq (InvokeApi GET "/swagger-ui/index.html").Status 200 "GET /swagger-ui/index.html"
AssertEq (InvokeApi GET "/v3/api-docs").Status 200 "GET /v3/api-docs"

Sect "2) Endpoints base"
AssertEq (InvokeApi GET "/api/autores").Status 200 "GET /api/autores"
AssertEq (InvokeApi GET "/api/generos").Status 200 "GET /api/generos"
AssertEq (InvokeApi GET "/api/libros").Status 200 "GET /api/libros"
AssertEq (InvokeApi GET "/api/usuarios").Status 200 "GET /api/usuarios"

Sect "3) Validacion de body (400)"
$r = InvokeApi POST "/api/autores" @{ nombre = ""; apellido = "X"; nacionalidad = "Cubana" }
AssertEq $r.Status 400 "POST /api/autores invalido"
if ($r.Body -match 'nombre') { Ok "el body menciona 'nombre'" } else { Fail "el body no menciona 'nombre'" }

Sect "4) Not found (404)"
$r = InvokeApi GET "/api/autores/999999"
AssertEq $r.Status 404 "GET /api/autores/999999"
if ($r.Body -match 'no encontrado') { Ok "mensaje 'no encontrado' en el body" } else { Fail "no hay mensaje esperado: $($r.Body)" }

Sect "5) Crear autor valido (201)"
$r = InvokeApi POST "/api/autores" @{ nombre = "Julio"; apellido = "Cortazar"; nacionalidad = "Argentina" }
AssertEq $r.Status 201 "POST /api/autores valido"
$autorId = ($r.Body | ConvertFrom-Json).idAutor
if ($autorId) { Ok "autor creado id=$autorId" } else { Fail "sin idAutor en respuesta" }

Sect "6) Crear libro valido (201)"
$r = InvokeApi POST "/api/libros" @{
    isbn = "9788437604947"
    titulo = "Cien anios de soledad"
    fechaPublicacion = "1967-05-30"
    numPaginas = 432
    cantidadEjemplares = 3
    sintesis = "Novela del realismo magico"
    idAutor = $autorId
    idGenero = 1
}
AssertEq $r.Status 201 "POST /api/libros valido"

Sect "7) DELETE autor con libros -> 409 (fix del bloque 1)"
$r = InvokeApi DELETE "/api/autores/$autorId"
AssertEq $r.Status 409 "DELETE /api/autores/$autorId (con libros)"
if ($r.Body -notmatch 'constraint|foreign key|prestamo|libro_autor') {
    Ok "sin leak de detalles tecnicos"
} else {
    Fail "LEAK: $($r.Body)"
}

Sect "8) DELETE autor limpio -> 204"
$r = InvokeApi POST "/api/autores" @{ nombre = "Temp"; apellido = "Test"; nacionalidad = "Test" }
$tempId = ($r.Body | ConvertFrom-Json).idAutor
AssertEq (InvokeApi DELETE "/api/autores/$tempId").Status 204 "DELETE autor sin libros"

Sect "9) Regla de negocio: prestamo sin ejemplares -> 409"
$u = (InvokeApi POST "/api/usuarios" @{ nombre = "Test"; apellido = "User"; email = "t@t.cu"; direccion = "Calle X" }).Body | ConvertFrom-Json
$u2 = (InvokeApi POST "/api/usuarios" @{ nombre = "Otro"; apellido = "User"; email = "o@o.cu"; direccion = "Calle Y" }).Body | ConvertFrom-Json
$null = InvokeApi POST "/api/libros" @{
    isbn = "9788437604954"; titulo = "Rayuela"; fechaPublicacion = "1963-06-28"
    numPaginas = 600; cantidadEjemplares = 1; sintesis = "Novela"
    idAutor = 1; idGenero = 1
}
AssertEq (InvokeApi POST "/api/prestamos" @{ idUsuario = $u.idUsuario; isbn = "9788437604954"; diasPrestamo = 7 }).Status 201 "POST prestamo 1 (con ejemplar)"
AssertEq (InvokeApi POST "/api/prestamos" @{ idUsuario = $u2.idUsuario; isbn = "9788437604954"; diasPrestamo = 7 }).Status 409 "POST prestamo 2 (sin ejemplares)"

Title "FASE 4 - Limpieza"

Stop-Process -Id $server.Id -Force -ErrorAction SilentlyContinue
Ok "servidor detenido"

Write-Host ""
Write-Host "===========================================" -ForegroundColor Magenta
$color = if ($script:fail -eq 0) { 'Green' } else { 'Red' }
Write-Host "  RESULTADO: $($script:pass) pasadas / $($script:fail) fallidas" -ForegroundColor $color
Write-Host "===========================================" -ForegroundColor Magenta

if ($script:fail -gt 0) {
    Write-Host ""
    Write-Host "Log del servidor: $logOut" -ForegroundColor Yellow
    exit 1
}
