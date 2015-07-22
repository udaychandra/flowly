eb.consumer('com.test.app1:1001', function(pMessage) {
    var instance = pMessage.body();
    var data = instance.data;
    var inputData = instance.inputData;
    if (inputData) {


    instance.inputData = null;
    }

    eb.send('io.flowly.engine:flow.instance.hop', pMessage.body());
});

