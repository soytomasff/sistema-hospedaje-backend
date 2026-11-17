param(
    [string]$DatabaseUrl = $(if ($env:DB_URL) { $env:DB_URL } else { 'jdbc:postgresql://localhost:5432/hospedaje_db' }),
    [string]$DatabaseUser = $(if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'postgres' }),
    [int]$Port = $(if ($env:PORT) { $env:PORT } else { 8080 })
)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path $PSScriptRoot -Parent
$originalLocation = Get-Location
$originalUrl = $env:DB_URL
$originalUser = $env:DB_USERNAME
$originalPort = $env:PORT
$prompted = $false
$backendExitCode = 1
try {
    Set-Location -LiteralPath $projectPath
    if (-not $env:DB_PASSWORD -and -not $env:SPRING_DATASOURCE_PASSWORD) {
        $securePassword = Read-Host 'Contraseña de PostgreSQL (no se guardará en archivos)' -AsSecureString
        $env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
        $prompted = $true
    }
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD) -and
        [string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_PASSWORD)) {
        throw 'La contrasena de PostgreSQL no puede estar vacia.'
    }
    $env:DB_URL = $DatabaseUrl
    $env:DB_USERNAME = $DatabaseUser
    $env:PORT = "$Port"
    & .\mvnw.cmd spring-boot:run
    $backendExitCode = $LASTEXITCODE
} finally {
    if ($prompted) { Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue }
    $env:DB_URL = $originalUrl
    $env:DB_USERNAME = $originalUser
    $env:PORT = $originalPort
    Set-Location -LiteralPath $originalLocation
}
exit $backendExitCode
