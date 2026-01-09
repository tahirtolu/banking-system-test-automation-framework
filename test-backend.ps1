# Backend Test Script
Write-Host "Testing Backend API..." -ForegroundColor Cyan

$uri = "http://localhost:8081/api/auth/register"
$body = @{
    username = "testuser123"
    password = "password123"
    email = "test123@test.com"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "5551234567"
} | ConvertTo-Json

Write-Host "`nAPI URL: $uri" -ForegroundColor Yellow
Write-Host "Request Body:" -ForegroundColor Yellow
Write-Host $body -ForegroundColor Gray

try {
    Write-Host "`nSending request..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri $uri -Method Post -ContentType "application/json" -Body $body
    Write-Host "`n✅ SUCCESS!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "`n❌ ERROR!" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Yellow
        }
    } else {
        Write-Host "No response received. Is backend running on port 8081?" -ForegroundColor Yellow
    }
}

Write-Host "`nTest completed." -ForegroundColor Cyan

