<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.parcel.model.*,com.parcel.util.Html" %>
<% Parcel parcel = (Parcel) request.getAttribute("parcel"); List<ParcelEvent> events = (List<ParcelEvent>) request.getAttribute("events"); String error = (String) request.getAttribute("error"); %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Parcel detail</title></head>
<body><h1>Parcel <%= Html.esc(parcel.id()) %></h1><p><a href="dashboard">Dashboard</a> | <a href="alerts">Alerts</a></p>
<p><%= Html.esc(parcel.origin()) %> to <%= Html.esc(parcel.destination()) %> | Status: <%= Html.esc(parcel.status()) %></p>
<% if (error != null) { %><p id="errorMsg"><%= Html.esc(error) %></p><% } %>
<form method="post" action="parcel"><input type="hidden" name="parcelId" value="<%= Html.esc(parcel.id()) %>">
<select id="eventStatus" name="eventStatus"><option>Picked Up</option><option>In Transit</option><option>Out for Delivery</option><option>Delivered</option><option>Failed</option></select>
<input id="eventLocation" name="eventLocation" placeholder="Location"><input id="eventTime" name="eventTime" type="datetime-local">
<input name="remarks" placeholder="Remarks"><button id="addEventBtn" type="submit">Add event</button></form>
<table id="eventTable"><tr><th>Status</th><th>Location</th><th>Time</th><th>Remarks</th></tr>
<% for (ParcelEvent event : events) { %><tr><td><%= Html.esc(event.status()) %></td><td><%= Html.esc(event.location()) %></td><td><%= Html.esc(event.eventTime()) %></td><td><%= Html.esc(event.remarks()) %></td></tr><% } %>
</table></body></html>
