<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.*,com.parcel.util.Html" %>
<%
    Parcel parcel = (Parcel) request.getAttribute("parcel");
    List<ParcelEvent> events = (List<ParcelEvent>) request.getAttribute("events");
    String notFound = (String) request.getAttribute("notFound");
    String trackId  = (String) request.getAttribute("trackId");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="description" content="Track your parcel status, route and delivery timeline.">
  <title>Track Parcel – Parcel Tracking Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
  <div class="page stack">
    <div class="panel">
      <h1>Track Your Parcel</h1>
      <p class="subtitle">Enter a parcel ID to see real-time status and delivery timeline.</p>
      <div class="top-nav">
        <a href="${pageContext.request.contextPath}/index.jsp">Home</a>
        <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
      </div>
    </div>

    <div class="panel">
      <form method="get" action="${pageContext.request.contextPath}/track" class="form-grid">
        <div class="field">
          <label for="trackId">Parcel ID</label>
          <input id="trackId" name="id" value="<%= Html.esc(trackId) %>" placeholder="e.g. DEMO-1001" autofocus>
        </div>
        <div class="actions">
          <button id="trackBtn" type="submit">Track</button>
        </div>
      </form>
    </div>

    <% if (notFound != null) { %>
    <div class="panel">
      <p id="trackNotFound" class="error">
        No parcel found with ID <strong><%= Html.esc(notFound) %></strong>.
        Please check the ID and try again.
      </p>
    </div>
    <% } %>

    <% if (parcel != null) { %>
    <div class="panel">
      <h2 class="section-title">Parcel <span id="trackResultId"><%= Html.esc(parcel.id()) %></span></h2>
      <dl class="detail-grid">
        <dt>Status</dt>
        <dd><span id="trackStatus" class="badge badge-<%= parcel.status().toLowerCase().replace(" ", "-") %>">
          <%= Html.esc(parcel.status()) %></span></dd>
        <dt>Route</dt>
        <dd id="trackRoute"><%= Html.esc(parcel.origin()) %> &rarr; <%= Html.esc(parcel.destination()) %></dd>
        <dt>Priority</dt>
        <dd><span id="trackPriority" class="badge badge-<%= Html.esc(parcel.priority().toLowerCase()) %>">
          <%= Html.esc(parcel.priority()) %></span></dd>
        <dt>Expected delivery</dt>
        <dd id="trackExpDelivery"><%= parcel.expectedDelivery() == null ? "Not specified" : Html.esc(parcel.expectedDelivery()) %></dd>
        <dt>Assigned agent</dt>
        <dd><%= parcel.assignedAgent() == null || parcel.assignedAgent().isBlank() ? "—" : Html.esc(parcel.assignedAgent()) %></dd>
      </dl>
    </div>

    <div class="panel">
      <h2 class="section-title">Event Timeline</h2>
      <% if (events == null || events.isEmpty()) { %>
      <p>No tracking events recorded yet.</p>
      <% } else { %>
      <table id="trackEventTable">
        <tr><th>Status</th><th>Location</th><th>Time</th><th>Remarks</th></tr>
        <% for (ParcelEvent ev : events) { %>
        <tr>
          <td><%= Html.esc(ev.status()) %></td>
          <td><%= Html.esc(ev.location()) %></td>
          <td><%= Html.esc(ev.eventTime()) %></td>
          <td><%= Html.esc(ev.remarks()) %></td>
        </tr>
        <% } %>
      </table>
      <% } %>
    </div>
    <% } %>
  </div>
</body>
</html>
