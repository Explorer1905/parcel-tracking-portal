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
  <style>
    body { font-family: Arial, sans-serif; margin: 30px; }
    form { margin-bottom: 25px; }
    label { display: inline-block; width: 110px; margin: 4px 0; }
    input { padding: 4px; width: 220px; }
    table { border-collapse: collapse; width: 100%; }
    th, td { border: 1px solid #999; padding: 6px 10px; text-align: left; }
    th { background: #e8eef7; }
    .error { color: #b00020; font-weight: bold; }
    .success { color: #1b7a2f; font-weight: bold; }
  </style>
</head>
<body>
  <h1>Parcel Tracking Portal</h1>
  <p><a href="index.jsp">Home</a></p>

  <h2>Add a new parcel</h2>
  <% if (error != null) { %><p id="errorMsg" class="error"><%= Html.esc(error) %></p><% } %>
  <% if (added != null) { %><p id="successMsg" class="success">Parcel <%= Html.esc(added) %> added successfully.</p><% } %>
  <form method="post" action="parcels">
    <label for="parcelId">Parcel ID</label>
    <input id="parcelId" name="parcelId" value="<%= Html.esc(request.getParameter("parcelId")) %>"><br>
    <label for="sender">Sender</label>
    <input id="sender" name="sender" value="<%= Html.esc(request.getParameter("sender")) %>"><br>
    <label for="receiver">Receiver</label>
    <input id="receiver" name="receiver" value="<%= Html.esc(request.getParameter("receiver")) %>"><br>
    <label for="origin">Origin</label>
    <input id="origin" name="origin" value="<%= Html.esc(request.getParameter("origin")) %>"><br>
    <label for="destination">Destination</label>
    <input id="destination" name="destination" value="<%= Html.esc(request.getParameter("destination")) %>"><br>
    <button id="addBtn" type="submit">Add parcel</button>
  </form>

  <h2>All parcels (<%= parcels.size() %>)</h2>
  <table id="parcelTable">
    <tr><th>Parcel ID</th><th>Sender</th><th>Receiver</th><th>Origin</th><th>Destination</th><th>Status</th><th>Created</th></tr>
    <% for (Parcel p : parcels) { %>
    <tr>
      <td><%= Html.esc(p.id()) %></td>
      <td><%= Html.esc(p.sender()) %></td>
      <td><%= Html.esc(p.receiver()) %></td>
      <td><%= Html.esc(p.origin()) %></td>
      <td><%= Html.esc(p.destination()) %></td>
      <td><%= Html.esc(p.status()) %></td>
      <td><%= Html.esc(p.createdAt()) %></td>
    </tr>
    <% } %>
  </table>
</body>
</html>