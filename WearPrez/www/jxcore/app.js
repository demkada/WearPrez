var fs = require('fs');

cordova('log').call("WearPrez is up and running!");

cordova('getBuffer').registerSync(function() {
  console.log("getBuffer is called!!!");
  var buffer = new Buffer(25000);
  buffer.fill(45);

  // send back a buffer
  return buffer;
});

cordova('asyncPing').registerAsync(function(message, callback){
  setTimeout(function() {
    callback("Pong:" + message);
  }, 500);
});

try {
// requiring a node module
  var jsnice = require('json-nice');

//using it
  var obj = {a: 1, b: 2};
  console.log(jsnice(obj));
} catch(e) {
  console.error("Seems like you didn't copy node_modules folder from sample/jxcore");
}

// execpath
console.log("execPath", process.execPath);

// cwd
console.log("process.cwd", process.cwd());

// iOS user directory
console.log("userPath", fs.readdirSync(process.userPath));

cordova('fromJXcore').registerToNative(function(param1, param2){
  // this method is reachable from Java or ObjectiveC
  // OBJ-C : [JXcore callEventCallback:@"fromJXcore" withParams:arr_parms];
  // Java  : jxcore.CallJSMethod("fromJXcore", arr_params);
});

// calling this custom native method from JXcoreExtension.m / .java
cordova('ScreenInfo').callNative(function(width, height){
  console.log("Size", width, height);
});

cordova('ScreenBrightness').callNative(function(br){
  console.log("Screen Brightness", br);
});

function getIP() {
var os = require('os');
var nets = os.networkInterfaces();
for ( var a in nets) {
var ifaces = nets[a];
for ( var o in ifaces) {
if (ifaces[o].family == "IPv4" && !ifaces[o].internal) { return ifaces[o].address; }
}
}
return null;
}
var ip = getIP();
if (!ip) {
console.error("You should connect to a network!");
return;
}
var http = require('http');
http.createServer(function(req, res) {
res.writeHead(200, {
'Content-Type': 'text/plain'
});
res.end('Hello from WearPrez '+Date.now()+'\n');
}).listen(7001, ip);
console.error('Server running at http://' + ip + ':7001/', "threadId", process.threadId);

