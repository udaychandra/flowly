data.sendTo = [];

for (var i=0; i<3; i++) {
    var to = "test" + i + "@test.com";

    data.sendTo.push(to);
}

data.subject = "Welcome to flowly";
data.message = "This is awesome!";