var eb = vertx.eventBus();

eb.consumer('com.test.app2:3001', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var inputData = instance.inputData;
    if (inputData) {


    instance.inputData = null;
    }

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

eb.consumer('com.test.app2:3003', function(pMessage) {
    eb.send('io.flowly.engine:user.interaction.view.start', pMessage.body());
});

eb.consumer('com.test.app2:3005', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;

    /* ############### Begin inline script: 1429372764302 ############### */
console.log(data.emailSubject);
console.log(data.emailBody);
    /* ############### End inline script ############### */

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

eb.consumer('com.test.app2:3007', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var outputData = {};

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

