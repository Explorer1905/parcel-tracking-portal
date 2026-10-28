<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.*,com.parcel.util.Html" %>
<% Parcel parcel = (Parcel) request.getAttribute("parcel"); List<ParcelEvent> events = (List<ParcelEvent>) request.getAttribute("events"); String error = (String) request.getAttribute("error"); %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Parcel detail</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
  <div class="page stack">
    <div class="panel">
      <h1>Parcel <%= Html.esc(parcel.id()) %></h1>
      <p class="subtitle">
        <%= Html.esc(parcel.origin()) %> to <%= Html.esc(parcel.destination()) %> | Status: <%= Html.esc(parcel.status()) %>
      </p>
      <div class="top-nav">
        <a href="index.jsp">Home</a>
        <a href="dashboard">Dashboard</a>
        <a href="alerts">Alerts</a>
      </div>
    </div>

    <div class="panel">
      <h2 class="section-title">Add tracking event</h2>
      <% if (error != null) { %><p id="errorMsg" class="error"><%= Html.esc(error) %></p><% } %>
      <form method="post" action="parcel" class="form-grid">
        <input type="hidden" name="parcelId" value="<%= Html.esc(parcel.id()) %>">
        <div class="field">
          <label for="eventStatus">Event status</label>
          <select id="eventStatus" name="eventStatus">
            <option>Picked Up</option>
            <option>In Transit</option>
            <option>Out for Delivery</option>
            <option>Delivered</option>
            <option>Failed</option>
          </select>
        </div>
        <div class="field">
          <label for="eventLocation">Location</label>
          <input id="eventLocation" name="eventLocation" placeholder="Location">
        </div>
        <div class="field">
          <label for="eventTime">Event time</label>
          <input id="eventTime" name="eventTime" type="datetime-local">
        </div>
        <div class="field">
          <label for="remarks">Remarks</label>
          <input id="remarks" name="remarks" placeholder="Remarks">
        </div>
        <div class="actions">
          <button id="addEventBtn" type="submit">Add event</button>
        </div>
      </form>
    </div>

    <div class="panel">
      <h2 class="section-title">Tracking timeline</h2>
      <table id="eventTable">
        <tr><th>Status</th><th>Location</th><th>Time</th><th>Remarks</th></tr>
        <% for (ParcelEvent event : events) { %>
        <tr>
          <td><%= Html.esc(event.status()) %></td>
          <td><%= Html.esc(event.location()) %></td>
          <td><%= Html.esc(event.eventTime()) %></td>
          <td><%= Html.esc(event.remarks()) %></td>
        </tr>
        <% } %>
      </table>
    </div>
  </div>
</body>
</html>
