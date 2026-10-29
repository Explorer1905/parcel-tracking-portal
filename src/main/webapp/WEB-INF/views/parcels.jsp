<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.Parcel,com.parcel.util.Html" %>
<%
    List<Parcel> parcels = (List<Parcel>) request.getAttribute("parcels");
    String error = (String) request.getAttribute("error");
    String added = request.getParameter("added");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Parcels - Parcel Tracking Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
  <div class="page stack">
    <div class="panel">
      <h1>Parcel Tracking Portal</h1>
      <p class="subtitle">Register parcels and view all records in one place.</p>
      <div class="top-nav">
        <a href="index.jsp">Home</a>
        <a href="track">Track</a>
        <a href="dashboard">Dashboard</a>
        <a href="alerts">Alerts</a>
        <a href="export/parcels.csv">Export CSV</a>
      </div>
    </div>

    <div class="panel">
      <h2 class="section-title">Add a new parcel</h2>
      <% if (error != null) { %><p id="errorMsg" class="error"><%= Html.esc(error) %></p><% } %>
      <% if (added != null) { %><p id="successMsg" class="success">Parcel <%= Html.esc(added) %> added successfully.</p><% } %>
      <form method="post" action="parcels" class="form-grid">
        <div class="field">
          <label for="parcelId">Parcel ID</label>
          <input id="parcelId" name="parcelId" value="<%= Html.esc(request.getParameter("parcelId")) %>">
        </div>
        <div class="field">
          <label for="sender">Sender</label>
          <input id="sender" name="sender" value="<%= Html.esc(request.getParameter("sender")) %>">
        </div>
        <div class="field">
          <label for="receiver">Receiver</label>
          <input id="receiver" name="receiver" value="<%= Html.esc(request.getParameter("receiver")) %>">
        </div>
        <div class="field">
          <label for="origin">Origin</label>
          <input id="origin" name="origin" value="<%= Html.esc(request.getParameter("origin")) %>">
        </div>
        <div class="field">
          <label for="destination">Destination</label>
          <input id="destination" name="destination" value="<%= Html.esc(request.getParameter("destination")) %>">
        </div>
        <%-- Optional V2 fields --%>
        <div class="field">
          <label for="weight">Weight (kg) <span class="hint">optional, default 1.0</span></label>
          <input id="weight" name="weight" type="number" step="0.01" min="0.01" max="1000"
                 placeholder="1.0" value="<%= Html.esc(request.getParameter("weight")) %>">
        </div>
        <div class="field">
          <label for="priority">Priority <span class="hint">optional</span></label>
          <select id="priority" name="priority">
            <option value="">Standard (default)</option>
            <option value="Standard"<% if ("Standard".equals(request.getParameter("priority"))) { %> selected<% } %>>Standard</option>
            <option value="Express"<% if ("Express".equals(request.getParameter("priority"))) { %> selected<% } %>>Express</option>
            <option value="Urgent"<% if ("Urgent".equals(request.getParameter("priority"))) { %> selected<% } %>>Urgent</option>
          </select>
        </div>
        <div class="field">
          <label for="expectedDelivery">Expected Delivery <span class="hint">optional</span></label>
          <input id="expectedDelivery" name="expectedDelivery" type="date"
                 value="<%= Html.esc(request.getParameter("expectedDelivery")) %>">
        </div>
        <div class="field">
          <label for="agent">Assigned Agent <span class="hint">optional</span></label>
          <input id="agent" name="agent" placeholder="Agent name"
                 value="<%= Html.esc(request.getParameter("agent")) %>">
        </div>
        <div class="actions">
          <button id="addBtn" type="submit">Add parcel</button>
        </div>
      </form>
    </div>

    <div class="panel">
      <h2 class="section-title">All parcels (<%= parcels.size() %>)</h2>
      <table id="parcelTable">
        <tr><th>Parcel ID</th><th>Sender</th><th>Receiver</th><th>Origin</th><th>Destination</th>
            <th>Priority</th><th>Weight&nbsp;(kg)</th><th>Expected</th><th>Status</th><th>Created</th></tr>
        <% for (Parcel p : parcels) { %>
        <tr>
          <td><%= Html.esc(p.id()) %></td>
          <td><%= Html.esc(p.sender()) %></td>
          <td><%= Html.esc(p.receiver()) %></td>
          <td><%= Html.esc(p.origin()) %></td>
          <td><%= Html.esc(p.destination()) %></td>
          <td><span class="badge badge-<%= Html.esc(p.priority().toLowerCase()) %>"><%= Html.esc(p.priority()) %></span></td>
          <td><%= p.weightKg() %></td>
          <td><%= p.expectedDelivery() == null ? "—" : Html.esc(p.expectedDelivery()) %></td>
          <td><%= Html.esc(p.status()) %></td>
          <td><%= Html.esc(p.createdAt()) %></td>
        </tr>
        <% } %>
      </table>
    </div>
  </div>
</body>
</html>