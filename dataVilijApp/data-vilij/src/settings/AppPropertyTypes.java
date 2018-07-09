package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    GRAPHICS_FILE_EXT,
    GRAPHICS_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,

    /*extra additions*/
    SNAPSHOT_PATH,
    CHART_NAME,
    DISPLAY,
    INVALID_FORMAT,
    INVALID_LINE,
    INVALID_COORDINATE,
    HW,
    VILIJ,
    RESOURCES,
    LINE_ERROR,
    REPEATED_NAME,
    READ_ONLY,
    NUMBER_OF_LINES,
    LINES_SHOWN,
    PIC_FORMAT,
    STYLE_SHEET,
    AVERAGE_LINE,
    CHART_SERIES,
    CUSTOM_SERIES,
    WARNING,
    DECIMAL,
    COMMA,
    DISPLAYSYMBOL,
    AT_SYMBOL,
    EMPTY,
    AT_ERROR,
    INVALID_INPUT,
    INVALID_NAME,

    DONE,
    NUMOFLABELS,
    CONTRUN,
    UPINTER,
    MAXITER,
    ALGOCONFI,
    EDIT,
    INSTOF,
    DATAFROM,
    LABELS,
    LABELSFROM,
    DASH,
    NULL,
    RANDOMCLUST,
    CLUST,
    RANDOMCLASS,
    CLASS,
    ALGO,
    EXIT_WHILE_RUNNING_WARNING,
    RUN_COMPLETE,
    ALGO_RUNNING,

    /*used for CSS*/
    CONFI,
    SAMPLE,
    BUTTONMANAGER,
    DATAMESS,
    ALGO_LIST,
    VBOX,
    ALGOTITLES,
    CONFI_BUTTON,
    PLAYBUTTON,
    EXIT_REQUEST,
    ALGORITHM_PATH,
    ALGORITHM_FOLDER,
    CLUST_CLASS,
    CLASSI_CLASS


}
