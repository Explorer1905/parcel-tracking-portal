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
  <meta charset="UTF-8"><title>Parcel Dashboard</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 30px; color: #20252b; }
    .cards { display: flex; gap: 12px; flex-wrap: wrap; margin: 20px 0; }
    .card { border: 1px solid #b7c4d4; border-radius: 6px; padding: 12px 18px; min-width: 110px; background: #eef4fb; }
    .number { display: block; font-size: 24px; font-weight: bold; }
    form { margin: 12px 0; } input, select { padding: 6px; margin: 3px; }
    table { border-collapse: collapse; width: 100%; margin-top: 12px; }
    th, td { border: 1px solid #999; padding: 7px; text-align: left; }
    th { background: #e8eef7; } .error { color: #b00020; font-weight: bold; }
    .alert { background: #fff1f0; border: 1px solid #d33; padding: 10px; }
  </style>
</head>
<body>
  <h1>Parcel Tracking Dashboard</h1>
  <p><a href="index.jsp">Home</a> | <a href="parcels">Add parcel</a></p>
  <div class="cards">
    <div class="card">Total<span class="number"><%= summary.total() %></span></div>
    <div class="card">Registered<span class="number"><%= summary.registered() %></span></div>
    <div class="card">In transit<span class="number"><%= summary.inTransit() %></span></div>
    <div class="card">Delivered<span class="number"><%= summary.delivered() %></span></div>
    <div class="card">Exceptions<span class="number"><%= summary.exceptions() %></span></div>
  </div>
  <% if (error != null) { %><p class="error"><%= Html.esc(error) %></p><% } %>
  <form method="get" action="dashboard">
    <label for="q">Search</label>
    <input id="q" name="q" value="<%= Html.esc((String) request.getAttribute("query")) %>"
           placeholder="ID, sender, receiver or location">
    <button type="submit">Search</button>
  </form>
  <h2>Search results (<%= parcels.size() %>)</h2>
  <table>
    <tr><th>Parcel</th><th>Route</th><th>Status</th><th>Drill-down</th></tr>
    <% for (Parcel p : parcels) { %>
    <tr>
      <td><%= Html.esc(p.id()) %></td>
      <td><%= Html.esc(p.origin()) %> to <%= Html.esc(p.destination()) %></td>
      <td><%= Html.esc(p.status()) %></td>
      <td><a href="dashboard?parcel=<%= Html.esc(p.id()) %>">View events</a></td>
    </tr>
    <% } %>
  </table>
  <% if (selected != null) { %>
    <h2>Event drill-down: <%= Html.esc(selected.id()) %></h2>
    <form method="post" action="dashboard">
      <input type="hidden" name="parcelId" value="<%= Html.esc(selected.id()) %>">
      <select name="status">
        <option>In Transit</option><option>Delivered</option>
        <option>Delayed</option><option>Exception</option>
      </select>
      <input name="location" placeholder="Location">
      <input name="remarks" placeholder="Remarks">
      <button type="submit">Record event</button>
    </form>
    <% if ("Delayed".equals(selected.status()) || "Exception".equals(selected.status())) { %>
      <p class="alert"><strong>Alert:</strong> this parcel requires attention.</p>
    <% } %>
    <table>
      <tr><th>Status</th><th>Location</th><th>Time</th><th>Remarks</th></tr>
      <% for (ParcelEvent event : events) { %>
      <tr><td><%= Html.esc(event.status()) %></td><td><%= Html.esc(event.location()) %></td>
          <td><%= Html.esc(event.eventTime()) %></td><td><%= Html.esc(event.remarks()) %></td></tr>
      <% } %>
    </table>
  <% } %>
</body>
</html>
