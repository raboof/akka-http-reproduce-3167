var fs = require("fs");
var https = require("https");

var CA_CERT = fs.readFileSync("src/main/resources/cert.pem")

let request = https.request({
    hostname : "localhost",
    port: 8443,
    path: "/",
    method: "GET",
    ca : CA_CERT
}, response => {

    response.on("error", console.error);
    response.on("data", data => console.log(Buffer.from(data).toString("utf-8")));

});

request.on("error", console.error);

request.end();
