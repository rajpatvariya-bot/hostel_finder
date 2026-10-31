$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$outDir = Join-Path $PSScriptRoot "generated"
if (-not (Test-Path $outDir)) {
  New-Item -ItemType Directory -Path $outDir | Out-Null
}

$pages = @(
  @{
    File = "db-tables-1.png"
    Subtitle = "Core identity, verification, and location management tables"
    Tables = @(
      @{
        Name = "users"
        Rows = @(
          @("id", "BIGINT", "PK", "Unique user identifier"),
          @("role", "ENUM", "", "User role: Admin, Owner, Student"),
          @("name", "VARCHAR(120)", "", "Full name of user"),
          @("email", "VARCHAR(180)", "", "Unique email address"),
          @("phone", "VARCHAR(20)", "", "Unique phone number"),
          @("password_hash", "VARCHAR(255)", "", "Encrypted password"),
          @("gender", "ENUM", "", "Gender of user"),
          @("age", "INT", "", "Age of user"),
          @("address", "VARCHAR(255)", "", "Address details"),
          @("status", "ENUM", "", "Current account status"),
          @("created_at", "DATETIME", "", "Account creation time")
        )
      },
      @{
        Name = "admins"
        Rows = @(
          @("user_id", "BIGINT", "PK, FK", "References admin user in users"),
          @("created_at", "DATETIME", "", "Admin record creation time")
        )
      },
      @{
        Name = "hostel_owners"
        Rows = @(
          @("user_id", "BIGINT", "PK, FK", "References owner in users"),
          @("owner_status", "ENUM", "", "Verification or approval status"),
          @("verified_at", "DATETIME", "", "Date and time of verification"),
          @("rejection_reason", "VARCHAR(400)", "", "Reason in case of rejection"),
          @("created_at", "DATETIME", "", "Owner record creation time")
        )
      },
      @{
        Name = "owner_documents"
        Rows = @(
          @("document_id", "BIGINT", "PK", "Unique document identifier"),
          @("owner_user_id", "BIGINT", "FK", "References hostel owner"),
          @("doc_type", "ENUM", "", "Type of uploaded document"),
          @("file_url", "VARCHAR(600)", "", "Path of uploaded file"),
          @("file_mime", "VARCHAR(100)", "", "File MIME type"),
          @("file_size_bytes", "BIGINT", "", "File size in bytes"),
          @("admin_status", "ENUM", "", "Approval state assigned by admin"),
          @("admin_note", "VARCHAR(400)", "", "Remarks from admin"),
          @("submitted_at", "DATETIME", "", "Submission timestamp")
        )
      },
      @{
        Name = "verification_reviews"
        Rows = @(
          @("review_id", "BIGINT", "PK", "Unique review identifier"),
          @("owner_user_id", "BIGINT", "FK", "References hostel owner"),
          @("admin_user_id", "BIGINT", "FK", "References admin account"),
          @("decision", "ENUM", "", "Review decision"),
          @("note", "VARCHAR(400)", "", "Additional review note"),
          @("created_at", "DATETIME", "", "Review creation time")
        )
      },
      @{
        Name = "otp_verifications"
        Rows = @(
          @("id", "BIGINT", "PK", "Unique OTP record identifier"),
          @("identifier", "VARCHAR(180)", "", "Email or mobile linked to OTP"),
          @("otp_code", "VARCHAR(255)", "", "Stored OTP value"),
          @("expiry_time", "DATETIME", "", "OTP expiry timestamp"),
          @("is_verified", "TINYINT(1)", "", "Verification status flag"),
          @("attempts", "INT", "", "Number of attempts made"),
          @("created_at", "DATETIME", "", "Record creation time")
        )
      }
    )
  },
  @{
    File = "db-tables-2.png"
    Subtitle = "Location, facilities, and hostel master tables"
    Tables = @(
      @{
        Name = "cities"
        Rows = @(
          @("city_id", "INT", "PK", "Unique city identifier"),
          @("name", "VARCHAR(80)", "", "Name of city")
        )
      },
      @{
        Name = "areas"
        Rows = @(
          @("area_id", "INT", "PK", "Unique area identifier"),
          @("city_id", "INT", "FK", "References related city"),
          @("name", "VARCHAR(80)", "", "Name of area or locality"),
          @("created_at", "DATETIME", "", "Area record creation time")
        )
      },
      @{
        Name = "facilities"
        Rows = @(
          @("facility_id", "INT", "PK", "Unique facility identifier"),
          @("name", "VARCHAR(60)", "", "Facility name")
        )
      },
      @{
        Name = "hostels"
        Rows = @(
          @("hostel_id", "BIGINT", "PK", "Unique hostel identifier"),
          @("owner_user_id", "BIGINT", "FK", "References hostel owner"),
          @("city_id", "INT", "FK", "References city"),
          @("area_id", "INT", "FK", "References area"),
          @("hostel_name", "VARCHAR(140)", "", "Name of hostel"),
          @("address_line", "VARCHAR(255)", "", "Full hostel address"),
          @("price_per_month", "DECIMAL(10,2)", "", "Monthly hostel fee"),
          @("gender_type", "ENUM", "", "Boys, girls, or co-ed"),
          @("mess_available", "TINYINT(1)", "", "Mess facility availability"),
          @("description", "TEXT", "", "Hostel description"),
          @("status", "ENUM", "", "Current publishing status"),
          @("total_rooms", "INT", "", "Total number of rooms"),
          @("available_rooms", "INT", "", "Rooms currently available"),
          @("created_at", "DATETIME", "", "Creation time"),
          @("updated_at", "DATETIME", "", "Latest update time")
        )
      },
      @{
        Name = "hostel_facilities"
        Rows = @(
          @("hostel_id", "BIGINT", "PK, FK", "References hostel"),
          @("facility_id", "INT", "PK, FK", "References facility")
        )
      },
      @{
        Name = "hostel_images"
        Rows = @(
          @("image_id", "BIGINT", "PK", "Unique image identifier"),
          @("hostel_id", "BIGINT", "FK", "References hostel"),
          @("file_url", "VARCHAR(600)", "", "Stored image path"),
          @("sort_order", "INT", "", "Display order of image"),
          @("created_at", "DATETIME", "", "Image insertion time")
        )
      }
    )
  },
  @{
    File = "db-tables-3.png"
    Subtitle = "Room management and inquiry processing tables"
    Tables = @(
      @{
        Name = "rooms"
        Rows = @(
          @("room_id", "BIGINT", "PK", "Unique room identifier"),
          @("hostel_id", "BIGINT", "FK", "References hostel"),
          @("room_number", "VARCHAR(30)", "", "Room number or label"),
          @("is_available", "TINYINT(1)", "", "Availability status"),
          @("is_active", "TINYINT(1)", "", "Operational room status"),
          @("created_at", "DATETIME", "", "Creation time")
        )
      },
      @{
        Name = "hostel_room_types"
        Rows = @(
          @("room_type_id", "BIGINT", "PK", "Unique room type identifier"),
          @("hostel_id", "BIGINT", "FK", "References hostel"),
          @("room_type", "VARCHAR(120)", "", "Name of room type"),
          @("price_per_month", "DECIMAL(10,2)", "", "Monthly rent for room type"),
          @("total_rooms", "INT", "", "Total count of this room type"),
          @("available_rooms", "INT", "", "Available count of this room type"),
          @("created_at", "DATETIME", "", "Creation time"),
          @("updated_at", "DATETIME", "", "Latest update time")
        )
      },
      @{
        Name = "inquiries"
        Rows = @(
          @("inquiry_id", "BIGINT", "PK", "Unique inquiry identifier"),
          @("hostel_id", "BIGINT", "FK", "References hostel"),
          @("room_type_id", "BIGINT", "FK", "References room type"),
          @("student_user_id", "BIGINT", "FK", "References student user"),
          @("requested_room_count", "INT", "", "Requested number of rooms"),
          @("message", "TEXT", "", "Inquiry message"),
          @("status", "ENUM", "", "Inquiry processing state"),
          @("owner_note", "VARCHAR(400)", "", "Note added by owner"),
          @("created_at", "DATETIME", "", "Creation time"),
          @("updated_at", "DATETIME", "", "Latest update time")
        )
      },
      @{
        Name = "inquiry_room_reservations"
        Rows = @(
          @("reservation_id", "BIGINT", "PK", "Unique reservation identifier"),
          @("inquiry_id", "BIGINT", "FK", "References inquiry"),
          @("room_id", "BIGINT", "FK", "References reserved room"),
          @("reserved_at", "DATETIME", "", "Reservation timestamp")
        )
      }
    )
  }
)

$width = 1800
$height = 2200

$bg = [System.Drawing.Color]::FromArgb(242, 247, 251)
$card = [System.Drawing.Color]::White
$line = [System.Drawing.Color]::FromArgb(206, 218, 232)
$headerBg = [System.Drawing.Color]::FromArgb(223, 235, 247)
$titleInk = [System.Drawing.Color]::FromArgb(22, 50, 79)
$muted = [System.Drawing.Color]::FromArgb(87, 106, 125)
$accent = [System.Drawing.Color]::FromArgb(31, 95, 153)

$titleFont = New-Object System.Drawing.Font("Segoe UI", 26, [System.Drawing.FontStyle]::Bold)
$subtitleFont = New-Object System.Drawing.Font("Segoe UI", 13, [System.Drawing.FontStyle]::Regular)
$cardTitleFont = New-Object System.Drawing.Font("Segoe UI", 18, [System.Drawing.FontStyle]::Bold)
$headFont = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
$bodyFont = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Regular)
$keyFont = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Bold)

$titleBrush = New-Object System.Drawing.SolidBrush($titleInk)
$mutedBrush = New-Object System.Drawing.SolidBrush($muted)
$accentBrush = New-Object System.Drawing.SolidBrush($accent)
$textBrush = New-Object System.Drawing.SolidBrush($titleInk)
$headerBrush = New-Object System.Drawing.SolidBrush($headerBg)
$whiteBrush = New-Object System.Drawing.SolidBrush($card)
$linePen = New-Object System.Drawing.Pen($line, 1)
$pkBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(29, 125, 77))
$fkBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(155, 93, 0))

function Draw-KeyTag {
  param(
    [System.Drawing.Graphics]$Graphics,
    [string]$Text,
    [float]$X,
    [float]$Y
  )

  if (-not $Text) { return }

  $parts = $Text -split ",\s*"
  [float]$offset = 0
  foreach ($part in $parts) {
    $size = $Graphics.MeasureString($part, $keyFont)
    [float]$rectX = $X + $offset
    [float]$rectWidth = [float]$size.Width + 16
    $rect = New-Object System.Drawing.RectangleF($rectX, $Y, $rectWidth, 22)
    $brush = if ($part -eq "PK") { $pkBrush } else { $fkBrush }
    $Graphics.FillRectangle($brush, $rect)
    $Graphics.DrawString($part, $keyFont, $whiteBrush, ($rectX + 8), ($Y + 3))
    $offset = $offset + [float]$size.Width + 24
  }
}

function Draw-TableCard {
  param(
    [System.Drawing.Graphics]$Graphics,
    [hashtable]$Table,
    [float]$X,
    [float]$Y,
    [float]$Width
  )

  $rowHeight = 30
  $titleHeight = 44
  $headerHeight = 30
  $padding = 12
  $cardHeight = $titleHeight + $headerHeight + ($Table.Rows.Count * $rowHeight) + 14

  $Graphics.FillRectangle($whiteBrush, $X, $Y, $Width, $cardHeight)
  $Graphics.DrawRectangle($linePen, $X, $Y, $Width, $cardHeight)
  $Graphics.FillRectangle($headerBrush, $X, $Y, $Width, $titleHeight)
  $Graphics.DrawString($Table.Name, $cardTitleFont, $accentBrush, $X + 14, $Y + 8)

  $headerY = $Y + $titleHeight
  $Graphics.FillRectangle($headerBrush, $X, $headerY, $Width, $headerHeight)

  [float]$col1 = $X + $padding
  [float]$col2 = $X + 220
  [float]$col3 = $X + 440
  [float]$col4 = $X + 610

  $Graphics.DrawString("COLUMN", $headFont, $textBrush, $col1, $headerY + 7)
  $Graphics.DrawString("TYPE", $headFont, $textBrush, $col2, $headerY + 7)
  $Graphics.DrawString("KEY", $headFont, $textBrush, $col3, $headerY + 7)
  $Graphics.DrawString("DESCRIPTION", $headFont, $textBrush, $col4, $headerY + 7)

  for ($i = 0; $i -lt $Table.Rows.Count; $i++) {
    $row = $Table.Rows[$i]
    $rowY = $headerY + $headerHeight + ($i * $rowHeight)
    $Graphics.DrawLine($linePen, $X, $rowY, $X + $Width, $rowY)
    $Graphics.DrawString($row[0], $bodyFont, $textBrush, $col1, $rowY + 6)
    $Graphics.DrawString($row[1], $bodyFont, $textBrush, $col2, $rowY + 6)
    Draw-KeyTag -Graphics $Graphics -Text $row[2] -X $col3 -Y ($rowY + 4)
    [float]$descWidth = $Width - 620
    $descRect = New-Object System.Drawing.RectangleF($col4, ($rowY + 4), $descWidth, ($rowHeight + 4))
    $Graphics.DrawString($row[3], $bodyFont, $textBrush, $descRect)
  }

  return $cardHeight
}

foreach ($page in $pages) {
  $bitmap = New-Object System.Drawing.Bitmap($width, $height)
  $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
  $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
  $graphics.Clear($bg)

  $graphics.FillRectangle($whiteBrush, 40, 32, 1720, 2130)
  $graphics.DrawRectangle($linePen, 40, 32, 1720, 2130)

  $graphics.DrawString("Hostel Finder System Database Tables", $titleFont, $titleBrush, 72, 66)
  $graphics.DrawString($page.Subtitle, $subtitleFont, $mutedBrush, 74, 112)

  $leftX = 72
  $rightX = 890
  $topY = 160
  $cardWidth = 790
  $leftY = $topY
  $rightY = $topY

  for ($i = 0; $i -lt $page.Tables.Count; $i++) {
    $table = $page.Tables[$i]
    if ($leftY -le $rightY) {
      $used = Draw-TableCard -Graphics $graphics -Table $table -X $leftX -Y $leftY -Width $cardWidth
      $leftY += $used + 20
    } else {
      $used = Draw-TableCard -Graphics $graphics -Table $table -X $rightX -Y $rightY -Width $cardWidth
      $rightY += $used + 20
    }
  }

  $outPath = Join-Path $outDir $page.File
  $bitmap.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
  $graphics.Dispose()
  $bitmap.Dispose()
}

Write-Output $outDir
