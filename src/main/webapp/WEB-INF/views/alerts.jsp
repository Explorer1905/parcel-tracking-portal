<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.Parcel,com.parcel.util.Html" %>
<% List<Parcel> alerts = (List<Parcel>) request.getAttribute("alerts"); %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Alerts</title></head>
<body><h1>Alerts and exceptions</h1><p><a href="dashboard">Dashboard</a></p>
<table id="alertTable"><tr><th>Parcel</th><th>Status</th><th>Route</th><th>Details</th></tr>
<% for (Parcel p : alerts) { %><tr><td><%= Html.esc(p.id()) %></td><td><%= Html.esc(p.status()) %></td><td><%= Html.esc(p.origin()) %> to <%= Html.esc(p.destination()) %></td><td><a href="parcel?id=<%= Html.esc(p.id()) %>">Open</a></td></tr><% } %>
</table></body></html>
