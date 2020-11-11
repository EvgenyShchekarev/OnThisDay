package com.onthisday.schekarev.onthisday.utils;

public class Const {

    public static class Date {
        public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
        public static final String DATE_FORMAT = "dd.MM.yyyy";
        public static final String DATE_DAY_MONTH = "dd.MM";
        public static final String DATE_YEAR = "yyyy";
        public static final String DAY_OF_WEEK = "EEEE";
        public static final String MONTH_STRING = "MMMM";
        public static final String DAY_INT = "dd";
    }

    public static class Argument {
        public static final String ARG_DATE = "date";
        public static final String ARG_EVENT = "event";
        public static final String ARG_IMAGE = "image";
    }

    public static class Dialog {
        public static final String DIALOG_DATE = "DialogDate";
        public static final String DIALOG_LOADING = "DialogLoading";
        public static final String DIALOG_OPTIONS = "DialogOptionsEvent";
        public static final String DIALOG_EDIT = "DialogEditEvent";
    }

    public static class Extra {
        public static final String EXTRA_EVENT_KEY = "com.onthisday.schekarev.onthisday.event_key";
        public static final String EXTRA_EVENT_DATE = "com.onthisday.schekarev.onthisday.date_event";
    }

    public static class Request {
        public static final int REQUEST_DATE = 0;
        public static final int REQUEST_IMAGE = 1;
        public static final int REQUEST_EVENT = 2;
    }

    public static class Tag {
        public static final String TAG_EVENT_LIST = "EventListFragment";
        public static final String TAG_PROFILE = "ProfileFragment";
        public static final String TAG_MAIN = "MainFragment";
        public static final String TAG_UTILS = "Utils";
    }

    public static class DB {
        public static final String DB_USERS = "Users";
        public static final String DB_EVENTS = "Events";
        public static final String DB_IMAGE = "images/";
        public static final String DB_EVENT_IMAGES = "/event_images/";
        public static final String DB_AVATARS_IMAGES = "/avatars_images/";
    }

    public static class Key {
        public static final String KEY_INDEX = "index";
    }
}
