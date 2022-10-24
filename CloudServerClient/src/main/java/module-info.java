module ru.bvkuchin.cloudserverclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.bvkuchin.cloudserverclient to javafx.fxml;
    exports ru.bvkuchin.cloudserverclient;
    exports ru.bvkuchin.cloudserverclient.controllers;
    opens ru.bvkuchin.cloudserverclient.controllers to javafx.fxml;
}