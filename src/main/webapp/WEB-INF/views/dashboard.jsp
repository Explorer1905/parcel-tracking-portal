<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.*,com.parcel.util.Html" %>
<%
    List<Parcel> parcels = (List<Parcel>) request.getAttribute("parcels");
    List<ParcelEvent> events = (List<ParcelEvent>) request.getAttribute("events");
    DashboardSummary summary = (DashboardSummary) request.getAttribute("summary");
    Parcel selected = (Parcel) request.getAttribute("selected");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Parcel Dashboard</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
  <div class="page stack">
    <div class="panel">
      <h1>Parcel Tracking Dashboard</h1>
      <p class="subtitle">Track operational status and drill into parcel movement events.</p>
      <div class="top-nav">
        <a href="index.jsp">Home</a>
        <a href="parcels">Add parcel</a>
        <a href="alerts">Alerts</a>
      </div>
    </div>

    <div class="panel">
      <div class="cards">
        <div class="card"><span class="card-label">Total</span><span id="sumTotal" class="number"><%= summary.total() %></span></div>
        <div class="card"><span class="card-label">In transit</span><span id="sumTransit" class="number"><%= summary.inTransit() %></span></div>
        <div class="card"><span class="card-label">Delivered</span><span id="sumDelivered" class="number"><%= summary.delivered() %></span></div>
        <div class="card"><span class="card-label">Delayed</span><span id="sumDelayed" class="number"><%= summary.delayed() %></span></div>
      </div>
    </div>

    <div class="panel">
      <% if (error != null) { %><p class="error"><%= Html.esc(error) %></p><% } %>
      <form method="get" action="dashboard" class="form-grid">
        <div class="field">
          <label for="searchBox">Search</label>
          <input id="searchBox" name="q" value="<%= Html.esc((String) request.getAttribute("query")) %>"
                 placeholder="ID, sender, receiver or location">
        </div>
        <div class="field">
          <label for="statusFilter">Status</label>
          <select id="statusFilter" name="status">
            <option value="">All statuses</option>
            <option>Picked Up</option>
            <option>In Transit</option>
            <option>Out for Delivery</option>
            <option>Delivered</option>
            <option>Failed</option>
          </select>
        </div>
        <div class="actions">
          <button id="searchBtn" type="submit">Search</button>
        </div>
      </form>
    </div>

    <div class="panel">
      <h2 class="section-title">Search results (<%= parcels.size() %>)</h2>
      <table id="parcelTable">
        <tr><th>Parcel</th><th>Route</th><th>Status</th><th>Drill-down</th></tr>
        <% for (Parcel p : parcels) { %>
        <tr>
          <td><%= Html.esc(p.id()) %></td>
          <td><%= Html.esc(p.origin()) %> to <%= Html.esc(p.destination()) %></td>
          <td><%= Html.esc(p.status()) %></td>
          <td><a class="link-btn" href="parcel?id=<%= Html.esc(p.id()) %>">View events</a></td>
        </tr>
        <% } %>
      </table>
    </div>
  </div>
</body>
</html>
