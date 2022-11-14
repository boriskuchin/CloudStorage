module ru.bvkuchin.cloudserverclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.common;


    opens ru.bvkuchin.cloudserverclient to javafx.fxml;
    exports ru.bvkuchin.cloudserverclient;
    exports ru.bvkuchin.cloudserverclient.controllers;
    opens ru.bvkuchin.cloudserverclient.controllers to javafx.fxml;
    exports ru.bvkuchin.cloudserverclient.net;
    opens ru.bvkuchin.cloudserverclient.net to javafx.fxml;
}