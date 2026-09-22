$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($env:RC_POSTFLOW_ADMIN_USERNAME) -or
    [string]::IsNullOrWhiteSpace($env:RC_POSTFLOW_ADMIN_PASSWORD)) {
    throw 'RC_POSTFLOW_ADMIN_USERNAME and RC_POSTFLOW_ADMIN_PASSWORD must be set for this process.'
}

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$gradleUserHome = Join-Path $repositoryRoot '.gradle'

Push-Location $repositoryRoot
try {
    $env:GRADLE_USER_HOME = $gradleUserHome
    & .\gradlew.bat bootRun '--args=--spring.profiles.active=screenshot'
    if ($LASTEXITCODE -ne 0) {
        throw "Screenshot environment exited with code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
