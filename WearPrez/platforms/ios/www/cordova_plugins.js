cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/io.jxcore.node/www/jxcore.js",
        "id": "io.jxcore.node.jxcore",
        "clobbers": [
            "jxcore"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-whitelist": "1.0.0",
    "io.jxcore.node": "0.0.1",
    "com.google.play.services": "25.0.0",
    "android.support.v7-appcompat": "3.0.0"
}
// BOTTOM OF METADATA
});