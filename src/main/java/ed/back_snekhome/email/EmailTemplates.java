package ed.back_snekhome.email;



public class EmailTemplates { // verification mail template, change password template


    private final static String logoLink = "http://localhost:8085/api/image/logo.png";

    private static String standardTemplate(String text, String link, String button) {
        return "<!DOCTYPE html> \n" +
                "<html lang=en> \n" +
                "<head> \n" +
                "    <meta charset=UTF-8> \n" +
                "    <title>Verify your email</title> \n" +
                "    <style> \n" +
                "    * { \n" +
                "        background-color: #2C2C4D;\n" +
                "        font-weight: bold; \n" +
                "        font-family: Arial, serif;\n" +
                "        color: #E3E3E3;\n" +
                "    } \n" +
                "    #logo img{ \n" +
                "        max-width: 100%; \n" +
                "        max-height: 100%; \n" +
                "    } \n" +
                "    #logo { \n" +
                "        width: 8%;\n" +
                "        margin-left: 15%;\n" +
                "    } \n" +
                "    #header { \n" +
                "        padding-top: 100px;\n" +
                "        font-size: 25px; \n" +
                "        display: flex; \n" +
                "    }\n" +
                "    #header h1 {\n" +
                "        margin-left: 21%;\n" +
                "    }\n" +
                "    #buttonDiv { \n" +
                "        font-size: 30px; \n" +
                "        margin-top: 40px;\n" +
                "        text-align: center;\n" +
                "    } \n" +
                "\n" +
                "    #button a {   \n" +
                "        text-decoration: none; \n" +
                "        transition: all 0.3s; \n" +
                "        background-color: #ff1177; \n" +
                "        color: white; \n" +
                "        border: 3px solid #ff1177; \n" +
                "        border-radius: 6px; \n" +
                "        padding: 10px;\n" +
                "        user-select: none;\n" +
                "    } \n" +
                "    #button a:hover { \n" +
                "        background-color: rgba(255, 17, 119, 0.7); \n" +
                "    } \n" +
                "    #button:active { \n" +
                "        margin-top: 4px;\n" +
                "    } \n" +
                "    #contentText { \n" +
                "        margin-top: 25px;  \n" +
                "        font-size: 20px; \n" +
                "        text-align: center;\n" +
                "    } \n" +
                "    #contentDiv { \n" +
                "        padding-bottom: 150px;  \n" +
                "    } \n" +
                "   \n" +
                "    </style> \n" +
                "</head> \n" +
                "<body> \n" +
                "\n" +
                "<div id=\"header\"> \n" +
                "    <div id=\"logo\"> \n" +
                "        <img src=\"" + logoLink + "\">\n" +
                "    </div> \n" +
                "    <h1>Hi buddy!</h1>\n" +
                "</div> \n" +
                "<div id=\"contentDiv\">\n" +
                "    <div id=\"contentText\">" +
                text +
                "    </div> \n" +
                "\n" +
                "    <div id=\"buttonDiv\"> \n" +
                "        <div id=\"button\">\n" +
                "            <a href=\"" + link + "\">" +
                button +
                "            </a> \n" +
                "        </div>\n" +
                "    </div> \n" +
                "</div>\n" +
                "</body> \n" +
                "</html>";
    }



    public static String verificationTemplate(String name, String link) {
        return standardTemplate(
                "Hello, " + name + ", if you have just registered on Snek-home, please confirm your email.",
                link,
                "Verify your account"
        );
    }


    public static String resetPasswordTemplate(String name, String link) {
        return standardTemplate(
                "Hello, " + name + ", if you requested to change your password, click below. Otherwise, ignore this mail.",
                link,
                "Reset password"
        );
    }


    public static String changeEmailTemplate(String name, String link, String email) {
        return standardTemplate(
                "Hello, " + name + ", if you requested to change your email to " + email + ", click a button below. Otherwise, don't do anything",
                link,
                "Confirm action"
        );
    }

    public static String changeEmailConfirmationNewTemplate(String name, String link) {
        return standardTemplate(
                "Hello, " + name + ", if you wanted to change e-mail on Snek-home, click a button below. Otherwise, don't do anything",
                link,
                "Confirm action"
        );
    }

}
