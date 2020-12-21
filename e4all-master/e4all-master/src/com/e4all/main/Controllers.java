package com.e4all.main;

class Controllers {

    private static MainController mainController;

    static MainController getMainController() {
        return mainController;
    }

    static void setMainController(MainController mainController) {
        Controllers.mainController = mainController;
    }
}