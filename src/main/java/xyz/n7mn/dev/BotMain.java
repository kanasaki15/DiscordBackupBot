package xyz.n7mn.dev;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BotMain {

    public static void main(String[] args) {

        File file = new File("./token.txt");
        if (!file.exists()){
            System.out.println("Tokenファイルがありません。\n自動生成しますのでその中にTokenを書いて保存してください。");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String token = null;

        try {
            FileInputStream fis = new FileInputStream("./token.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            String content;
            StringBuffer sb = new StringBuffer();
            while((content = bf.readLine()) != null) {
                sb.append(content);
                sb.append("\n");
            }
            token = sb.toString();
            bf.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        if (token == null){
            System.out.println("Tokenが設定されていません。");
            return;
        }

        token = token.replaceAll("\n", "");

        JDA build = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new Listener())
                .enableCache(CacheFlag.VOICE_STATE)
                .enableCache(CacheFlag.EMOJI)
                .enableCache(CacheFlag.STICKER)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();


    }
}
