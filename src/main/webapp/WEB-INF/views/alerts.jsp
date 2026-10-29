<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.Parcel,com.parcel.util.Html" %>
<% List<Parcel> alerts = (List<Parcel>) request.getAttribute("alerts"); %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Alerts</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
  <div class="page stack">
    <div class="panel">
      <h1>Alerts and exceptions</h1>
      <p class="subtitle">Review delayed or failed deliveries that require attention.</p>
      <div class="top-nav">
        <a href="index.jsp">Home</a>
        <a href="track">Track</a>
        <a href="dashboard">Dashboard</a>
        <a href="parcels">Parcels</a>
      </div>
    </div>

    <div class="panel">
      <table id="alertTable">
        <tr><th>Parcel</th><th>Status</th><th>Route</th><th>Details</th></tr>
        <% for (Parcel p : alerts) { %>
        <tr>
          <td><%= Html.esc(p.id()) %></td>
          <td><%= Html.esc(p.status()) %></td>
          <td><%= Html.esc(p.origin()) %> to <%= Html.esc(p.destination()) %></td>
          <td><a class="link-btn" href="parcel?id=<%= Html.esc(p.id()) %>">Open</a></td>
        </tr>
        <% } %>
      </table>
    </div>
  </div>
</body>
</html>
