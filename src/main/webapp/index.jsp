<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="description" content="Parcel Tracking Portal – register, track and monitor parcel movement.">
  <title>Parcel Tracking Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
  <div class="page">
    <div class="panel">
      <h1>Parcel Tracking Portal</h1>
      <p class="subtitle">
        Register, track and monitor parcel movement through a clean operations dashboard.
      </p>
      <div class="top-nav">
        <a href="track">🔍 Track a Parcel</a>
        <a href="parcels">Register Parcel</a>
        <a href="dashboard">Dashboard</a>
        <a href="alerts">Alerts</a>
        <a href="export/parcels.csv">⬇ Export CSV</a>
        <a href="api/summary">API Summary</a>
        <a href="health">Health</a>
      </div>
    </div>
  </div>
</body>
</html>