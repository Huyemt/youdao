package huyemt;

import huyemt.json.TransJson;
import huyemt.json.TransValue;
import javaocr.JavaOcr;
import org.huyemt.crypto4j.Crypto4J;
import org.huyemt.http4j.Http4J;
import org.huyemt.http4j.HttpResponse;
import org.huyemt.http4j.resource.Cookies;
import org.huyemt.http4j.resource.Headers;
import org.huyemt.http4j.resource.RequestBody;
import org.huyemt.json4j.Json4J;
import org.rifle.Rifle;
import org.rifle.command.Command;
import org.rifle.command.arguments.Argument;
import org.rifle.module.Module;
import org.rifle.scheduler.Task;
import org.rifle.utils.TextFormat;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有道翻译爬虫
 *
 * @author Huyemt
 */

public class Youdao extends Module {
    @Override
    protected String getModuleName() {
        return "YouDao";
    }

    @Override
    protected String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    protected String[] getModuleAuthors() {
        return new String[]{"Huyemt"};
    }

    @Override
    protected String getModuleStringDescription() {
        return "基于Http4J的有道翻译爬虫";
    }

    @Override
    public boolean isUserCanSelect() {
        return false;
    }

    private static Youdao instance;

    public Youdao() {
        instance = this;
    }

    @Override
    public void onLoad() {
        getLogger().info("\n\t" + TextFormat.FONT_RED + TextFormat.STYLE_BOLD + "欢迎使用有道翻译爬虫" + TextFormat.RESET + "\n\t作者 -> " + TextFormat.STYLE_BOLD + TextFormat.FONT_YELLOW + "Huyemt (楠生)" + TextFormat.RESET + "\n\t网络请求库 -> Http4J (Rifle内置)\n\t密码加密库 -> Crypto4J (Rifle内置)\n\n" + TextFormat.FONT_GREEN + TextFormat.STYLE_BOLD + "现在您可以在全局使用翻译指令");
        Rifle.getInstance().getCommandManager().register(new YoudaoCommand());
    }

    public static Youdao getInstance() {
        return instance;
    }
}

class YoudaoCommand extends Command {
    private static final Pattern pattern = Pattern.compile("n\\.md5\\(\"fanyideskweb\" \\+ e \\+ i \\+ \"(.*?)\"\\)");

    public YoudaoCommand() {
        super("youdao", "有道翻译爬虫", new String[]{"youdao <value>"}, true);
    }

    @Override
    public void execute(Argument argument) {
        if (argument.getOrigin().length() == 0) {
            Youdao.getInstance().getLogger().println(TextFormat.FONT_RED + "无翻译内容");
            return;
        }

        String value = argument.getOrigin().trim();

        Task task = new Task() {
            @Override
            public void run() {
                Youdao.getInstance().getLogger().println("\n" + translate(value));
            }
        };
        Rifle.getInstance().getScheduler().addTask(task);
    }

    private String translate(String value) {
        try {

            Matcher matcher = pattern.matcher(Http4J.get("https://shared.ydstatic.com/fanyi/newweb/v1.1.10/scripts/newweb/fanyi.min.js").html);
            if (matcher.find()) {
                String key = matcher.group(1);

                String unixtime = String.valueOf(new Date().getTime());

                RequestBody requestBody = new RequestBody();
                requestBody
                        .add("i", value)
                        .add("from", "AUTO")
                        .add("to", "AUTO")
                        .add("smartresult", "dict")
                        .add("client", "fanyideskweb")
                        .add("salt", unixtime + new Random().nextInt(10))
                        .add("sign", Crypto4J.MD5.encrypt("fanyideskweb" + requestBody.get("i") + requestBody.get("salt") + key))
                        .add("lts", unixtime)
                        .add("bv", Crypto4J.MD5.encrypt("5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36"))
                        .add("doctype", "json")
                        .add("version", "2.1")
                        .add("keyfrom", "fanyi.web")
                        .add("action", "FY_BY_REALTlME");

                HttpResponse response = Http4J.post(
                        "https://fanyi.youdao.com/translate_o",
                        new Headers()
                                .add("Origin", "https://fanyi.youdao.com")
                                .add("Referer", "https://fanyi.youdao.com/")
                                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
                                .add("Cookie", "OUTFOX_SEARCH_USER_ID=-528992281@183.48.139.37; OUTFOX_SEARCH_USER_ID_NCOO=1651091875.243739; _ntes_nnid=7ef72f194b7be04d4306d15a3e9826f0,1625824911320; ___rl__test__cookies=" + unixtime),
                        requestBody
                );


                TransJson json = Json4J.parse(response.html, TransJson.class);
                if (json.translateResult == null) {
                    return value + "\n";
                }
                StringBuilder result = new StringBuilder();
                for (LinkedList<TransValue> line : json.translateResult) {
                    for (TransValue value1 : line) {
                        result.append(value1).append(" ");
                    }
                    result.append("\n");
                }

                return result.toString();
            } else
                return "未找到Key";
        } catch (IOException e) {
            return TextFormat.FONT_RED + "~[~发生错误~]~";
        }
    }
}