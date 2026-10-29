'use strict';
var map = L.map('map', {zoomControl:false, worldCopyJump:true, minZoom:2, maxZoom:18}).setView([39,32],5);
L.control.zoom({position:'topright'}).addTo(map);

function resizeMap() { if (map) map.invalidateSize(); }
setTimeout(resizeMap, 100);
setTimeout(resizeMap, 400);
setTimeout(resizeMap, 1000);
window.addEventListener('resize', resizeMap);

var imagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
 maxZoom:18, attribution:'Imagery © Esri, Maxar, Earthstar Geographics'}).addTo(map);
var labels = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}', {
 maxZoom:18, attribution:'Labels © Esri'}).addTo(map);

var statusTimer;
imagery.on('tileerror', function(e) {
 var s = document.getElementById('status');
 s.textContent = 'Uydu görüntüsü yüklenemedi. Bağlantınızı kontrol edin.';
 s.style.display = 'block';
 clearTimeout(statusTimer);
 statusTimer = setTimeout(function() { s.style.display = 'none'; }, 8000);
});

var weatherLayer = L.layerGroup().addTo(map);
var routeLayer = L.layerGroup().addTo(map);
var selectedLayer = L.layerGroup().addTo(map);
var routeRevision = null;
var flightAnimId = null;

function wrapLongitude(lng) {
 return ((lng + 180) % 360 + 360) % 360 - 180;
}

function calculateBearing(lat1, lon1, lat2, lon2) {
 var rad = Math.PI / 180;
 var deg = 180 / Math.PI;
 var dLon = (lon2 - lon1) * rad;
 var y = Math.sin(dLon) * Math.cos(lat2 * rad);
 var x = Math.cos(lat1 * rad) * Math.sin(lat2 * rad) -
         Math.sin(lat1 * rad) * Math.cos(lat2 * rad) * Math.cos(dLon);
 return (Math.atan2(y, x) * deg + 360) % 360;
}

function choosePoint(lat, lng) {
 lng = wrapLongitude(lng);
 selectedLayer.clearLayers();
 L.marker([lat, lng], {
  icon: L.divIcon({
   className: '',
   html: '<div class="selected-dot"></div>',
   iconSize: [22, 22],
   iconAnchor: [11, 11]
  })
 }).addTo(selectedLayer);
 if (window.Metaro) Metaro.selectPoint(lat, lng);
}

map.on('click', function(e) {
 choosePoint(e.latlng.lat, e.latlng.lng);
});

function viewport() {
 var bounds = map.getBounds(), points = [];
 var south = Math.max(-84, bounds.getSouth()), north = Math.min(84, bounds.getNorth());
 var west = bounds.getWest(), east = bounds.getEast();
 var span = Math.min(340, east - west);
 var center = map.getCenter().lng;
 west = center - span / 2;
 for (var row = 0; row < 2; row++) {
  for (var col = 0; col < 3; col++) {
   points.push({
    latitude: Number((south + (north - south) * (row + .5) / 2).toFixed(3)),
    longitude: Number(wrapLongitude(west + span * (col + .5) / 3).toFixed(3))
   });
  }
 }
 if (window.Metaro) Metaro.viewport(JSON.stringify(points));
}

map.on('moveend', viewport);

function icon(code) {
 if (code === 0) return '☀';
 if (code >= 1 && code <= 3) return '☁';
 if (code === 45 || code === 48) return '≋';
 if ((code >= 71 && code <= 77) || code === 85 || code === 86) return '❄';
 if (code >= 95) return 'ϟ';
 if (code >= 51 && code <= 82) return '☂';
 return '·';
}

window.setWeather = function(items) {
 weatherLayer.clearLayers();
 items.forEach(function(item) {
  var w = item.weather, p = item.point;
  if (w.temperature === null || w.temperature === undefined) return;
  var el = document.createElement('div');
  el.className = 'weather-chip';
  el.textContent = icon(w.code) + ' ' + Math.round(w.temperature) + '°';
  var label = document.createElement('small');
  label.textContent = Number(p.latitude).toFixed(1) + ' · ' + Number(p.longitude).toFixed(1);
  el.appendChild(label);
  var marker = L.marker([p.latitude, p.longitude], {
   icon: L.divIcon({
    className: '',
    html: el,
    iconSize: [76, 42],
    iconAnchor: [38, 21]
   })
  });
  marker.on('click', function() { choosePoint(p.latitude, p.longitude); });
  marker.addTo(weatherLayer);
 });
};

// Spherical interpolation follows a great circle and unwraps the antimeridian.
function greatCircle(a, b) {
 var rad = Math.PI / 180;
 function vector(p) {
  var lat = p[0] * rad, lon = p[1] * rad;
  return [Math.cos(lat) * Math.cos(lon), Math.cos(lat) * Math.sin(lon), Math.sin(lat)];
 }
 var u = vector(a), v = vector(b);
 var dot = Math.max(-1, Math.min(1, u.reduce(function(s, x, i) { return s + x * v[i]; }, 0)));
 var omega = Math.acos(dot), sin = Math.sin(omega), points = [];
 var ortho;
 if (dot < -0.999999) {
  var axis = Math.abs(u[2]) < 0.9 ? [0, 0, 1] : [0, 1, 0];
  ortho = [u[1] * axis[2] - u[2] * axis[1], u[2] * axis[0] - u[0] * axis[2], u[0] * axis[1] - u[1] * axis[0]];
  var length = Math.hypot.apply(null, ortho);
  ortho = ortho.map(function(x) { return x / length; });
 }
 for (var i = 0; i <= 96; i++) {
  var t = i / 96;
  var q = omega < 1e-8 ? u : ortho ? u.map(function(x, j) {
   return x * Math.cos(Math.PI * t) + ortho[j] * Math.sin(Math.PI * t);
  }) : u.map(function(x, j) {
   return Math.sin((1 - t) * omega) / sin * x + Math.sin(t * omega) / sin * v[j];
  });
  var lat = Math.atan2(q[2], Math.hypot(q[0], q[1])) / rad;
  var lon = Math.atan2(q[1], q[0]) / rad;
  if (points.length) {
   while (lon - points[points.length - 1][1] > 180) lon -= 360;
   while (lon - points[points.length - 1][1] < -180) lon += 360;
  }
  points.push([lat, lon]);
 }
 return points;
}

window.setRoute = function(route) {
 if (flightAnimId) {
  cancelAnimationFrame(flightAnimId);
  flightAnimId = null;
 }

 if (!route) {
  routeLayer.clearLayers();
  routeRevision = null;
  return;
 }

 if (route.revision === routeRevision) return;
 routeRevision = route.revision;
 routeLayer.clearLayers();
 selectedLayer.clearLayers();

 var a = route.departure;
 var b = route.arrival;
 var path = greatCircle([a.latitude, a.longitude], [b.latitude, b.longitude]);

 // Glow background route line
 L.polyline(path, { color: '#71e8db', weight: 6, opacity: 0.35, className: 'animated-route-bg' }).addTo(routeLayer);
 // Flowing dashed route line
 L.polyline(path, { color: '#a5efdb', weight: 3, opacity: 0.95, className: 'animated-route-flow' }).addTo(routeLayer);

 // Departure (Kalkış) Badge with Radar Wave Beacon and Climb Animation
 var depWrap = document.createElement('div');
 depWrap.className = 'endpoint-wrapper';
 depWrap.innerHTML = '<div class="radar-pulse"></div>' +
  '<div class="endpoint endpoint-takeoff">' +
  '<span class="endpoint-icon takeoff-bounce">🛫</span>' +
  '<span>' + a.icao + '</span>' +
  '<span class="endpoint-tag">KALKIŞ</span>' +
  '</div>';

 L.marker(path[0], {
  icon: L.divIcon({
   className: '',
   html: depWrap,
   iconSize: [100, 36],
   iconAnchor: [50, 18]
  }),
  zIndexOffset: 500
 }).on('click', function() {
  if (window.Metaro) Metaro.selectEndpoint(true);
 }).addTo(routeLayer);

 // Arrival (İniş) Badge with Radar Wave Beacon and Descent Animation
 var arrWrap = document.createElement('div');
 arrWrap.className = 'endpoint-wrapper';
 arrWrap.innerHTML = '<div class="radar-pulse landing-pulse"></div>' +
  '<div class="endpoint endpoint-landing">' +
  '<span class="endpoint-icon landing-bounce">🛬</span>' +
  '<span>' + b.icao + '</span>' +
  '<span class="endpoint-tag">İNİŞ</span>' +
  '</div>';

 L.marker(path[path.length - 1], {
  icon: L.divIcon({
   className: '',
   html: arrWrap,
   iconSize: [100, 36],
   iconAnchor: [50, 18]
  }),
  zIndexOffset: 500
 }).on('click', function() {
  if (window.Metaro) Metaro.selectEndpoint(false);
 }).addTo(routeLayer);

 // Flying Aircraft in continuous Takeoff -> Cruise -> Landing animation loop
 var planeBox = document.createElement('div');
 planeBox.className = 'flying-plane-box';
 planeBox.innerHTML = '<svg class="flying-plane-svg" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">' +
  '<path d="M24 3 L27 15 L43 27 L43 31 L27 25 L27 38 L33 43 L33 46 L24 43 L15 46 L15 43 L21 38 L21 25 L5 31 L5 27 L21 15 Z" fill="#FFFFFF" stroke="#071521" stroke-width="1.8" stroke-linejoin="round"/>' +
  '<path d="M22 8 L26 8 L25.5 13 L22.5 13 Z" fill="#71e8db"/>' +
  '<circle cx="43" cy="29" r="2.2" fill="#52FF80"/>' +
  '<circle cx="5" cy="29" r="2.2" fill="#FF5252"/>' +
  '<circle cx="24" cy="43" r="2" fill="#FFE066"/>' +
  '</svg>' +
  '<div class="plane-jet-trail"></div>';

 var planeMarker = L.marker(path[0], {
  icon: L.divIcon({
   className: '',
   html: planeBox,
   iconSize: [48, 48],
   iconAnchor: [24, 24]
  }),
  zIndexOffset: 1200
 }).addTo(routeLayer);

 var FLIGHT_DURATION = 7000; // 7 seconds flight
 var PAUSE_DURATION = 1500;  // 1.5 seconds landing pause before new takeoff
 var TOTAL_CYCLE = FLIGHT_DURATION + PAUSE_DURATION;
 var startTime = null;

 function animateFlight(timestamp) {
  if (!startTime) startTime = timestamp;
  var elapsed = (timestamp - startTime) % TOTAL_CYCLE;

  if (elapsed <= FLIGHT_DURATION) {
   var p = elapsed / FLIGHT_DURATION;
   var floatIdx = p * (path.length - 1);
   var i1 = Math.floor(floatIdx);
   var i2 = Math.min(path.length - 1, i1 + 1);
   var frac = floatIdx - i1;

   var curLat = path[i1][0] + (path[i2][0] - path[i1][0]) * frac;
   var curLng = path[i1][1] + (path[i2][1] - path[i1][1]) * frac;

   var nextIdx = Math.min(path.length - 1, i1 + 2);
   var bearing = calculateBearing(path[i1][0], path[i1][1], path[nextIdx][0], path[nextIdx][1]);

   // Takeoff climb (0.0 to 0.18): scale climbs from 0.55 to 1.15
   // Cruise (0.18 to 0.82): scale is 1.15, full altitude
   // Landing descent (0.82 to 1.0): scale descends from 1.15 to 0.55
   var scale = 1.15;
   var shadowY = 6;
   var shadowBlur = 8;
   if (p < 0.18) {
    var t = p / 0.18;
    scale = 0.55 + t * 0.6;
    shadowY = 1 + t * 5;
    shadowBlur = 2 + t * 6;
   } else if (p > 0.82) {
    var t = (p - 0.82) / 0.18;
    scale = 1.15 - t * 0.6;
    shadowY = 6 - t * 5;
    shadowBlur = 8 - t * 6;
   }

   planeMarker.setLatLng([curLat, curLng]);
   planeBox.style.transform = 'rotate(' + bearing.toFixed(1) + 'deg) scale(' + scale.toFixed(2) + ')';
   var svg = planeBox.querySelector('.flying-plane-svg');
   if (svg) {
    svg.style.filter = 'drop-shadow(0 ' + shadowY.toFixed(0) + 'px ' + shadowBlur.toFixed(0) + 'px rgba(0,0,0,0.85)) drop-shadow(0 0 10px rgba(113,232,219,0.9))';
   }
  } else {
   // Landed at arrival airport touchdown zone
   planeMarker.setLatLng(path[path.length - 1]);
   var lastBearing = calculateBearing(path[path.length - 2][0], path[path.length - 2][1], path[path.length - 1][0], path[path.length - 1][1]);
   planeBox.style.transform = 'rotate(' + lastBearing.toFixed(1) + 'deg) scale(0.55)';
  }

  flightAnimId = requestAnimationFrame(animateFlight);
 }

 flightAnimId = requestAnimationFrame(animateFlight);
 map.fitBounds(L.latLngBounds(path), { padding: [55, 60], maxZoom: 10 });
};

if (window.Metaro) {
 Metaro.ready();
 viewport();
}
