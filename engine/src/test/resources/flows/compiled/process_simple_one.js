var eb = vertx.eventBus();

eb.consumer('com.test.app1:1001', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var inputData = instance.inputData;
    if (inputData) {


    instance.inputData = null;
    }

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

eb.consumer('com.test.app1:1003', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;

    /* ############### Begin inline script: 1429372764270 ############### */
data.sendTo = [];

for (var i=0; i<3; i++) {
    var to = "test" + i + "@test.com";

    data.sendTo.push(to);
}

data.subject = "Welcome to flowly";
data.message = "This is awesome!";
    /* ############### End inline script ############### */

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

eb.consumer('com.test.app1:1005', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var outputData = {};

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

