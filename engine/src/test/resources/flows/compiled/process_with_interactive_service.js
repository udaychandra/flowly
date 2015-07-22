var eb = vertx.eventBus();

eb.consumer('com.test.app2:2001', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var inputData = instance.inputData;
    if (inputData) {


    instance.inputData = null;
    }

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

eb.consumer('com.test.app2:2003', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var inputData = {};

    eb.send('io.flowly.engine:user.interaction.await', instance);
});

eb.consumer('com.test.app2:2003-io.flowly.engine:flow.instance.hopOut', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var outputData = instance.outputData;

    instance.outputData = null;
    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

eb.consumer('com.test.app2:2005', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var outputData = {};

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

