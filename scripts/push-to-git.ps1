# Helper script to initialize a git repo and push to remote
# Usage: PowerShell -ExecutionPolicy Bypass -File .\scripts\push-to-git.ps1

function Write-ErrAndExit($msg) {
    Write-Host "ERROR: $msg" -ForegroundColor Red
    exit 1
}

# Check for git
try {
    $gitVer = & git --version 2>$null
} catch {
    $gitVer = $null
}

if (-not $gitVer) {
    Write-Host "Git is not found in PATH." -ForegroundColor Yellow
    Write-Host "Install Git for Windows from https://git-scm.com/download/win or use winget:"
    Write-Host "  winget install --id Git.Git -e --source winget" -ForegroundColor Cyan
    Write-Host "After installing, re-run this script. Exiting." -ForegroundColor Yellow
    exit 1
}

Write-Host "Found $gitVer" -ForegroundColor Green

Push-Location -Path (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Push-Location -Path ".." | Out-Null  # move to repo root (scripts/..)
$repoRoot = Get-Location
Write-Host "Working in $repoRoot"

# Initialize repo if needed
$inside = $true
try {
    $inside = (& git rev-parse --is-inside-work-tree) -eq 'true'
} catch {
    $inside = $false
}

if (-not $inside) {
    Write-Host "Initializing new git repository..."
    git init || Write-ErrAndExit "git init failed"
} else {
    Write-Host "Already inside a git repository." -ForegroundColor Green
}

# Ensure .gitignore exists
if (-not (Test-Path -Path ".gitignore")) {
    Write-Host "No .gitignore found. You may want to add one before committing." -ForegroundColor Yellow
}

# Stage files
Write-Host "Staging files..."
git add . || Write-ErrAndExit "git add failed"

# Commit if there are staged changes
$hasChanges = (& git status --porcelain).Trim()
if ($hasChanges) {
    Write-Host "Committing changes..."
    git commit -m "Initial commit" || Write-ErrAndExit "git commit failed"
} else {
    Write-Host "No changes to commit." -ForegroundColor Yellow
}

# Ensure branch is main
try {
    git branch --show-current | Out-Null
    git branch -M main 2>$null || Write-Host "Could not rename branch to main (maybe already exists)." -ForegroundColor Yellow
} catch {
    # ignore
}

$remoteUrl = Read-Host "Enter remote URL to add (leave empty to skip push)"
if ([string]::IsNullOrWhiteSpace($remoteUrl)) {
    Write-Host "Skipping remote add/push. Done." -ForegroundColor Green
    Pop-Location; Pop-Location
    exit 0
}

# Add or update remote
try {
    $existing = (& git remote get-url origin) 2>$null
    if ($existing) {
        Write-Host "Remote 'origin' already exists. Updating URL to $remoteUrl"
        git remote set-url origin $remoteUrl || Write-ErrAndExit "git remote set-url failed"
    } else {
        git remote add origin $remoteUrl || Write-ErrAndExit "git remote add failed"
    }
} catch {
    git remote add origin $remoteUrl 2>$null || Write-ErrAndExit "git remote add failed"
}

# Push
Write-Host "Pushing to origin main..."
git push -u origin main || Write-ErrAndExit "git push failed. You may need to authenticate or create the remote repository first."

Write-Host "Push completed." -ForegroundColor Green
Pop-Location; Pop-Location
