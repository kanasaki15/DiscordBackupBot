package xyz.n7mn.dev;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Listener extends ListenerAdapter {

    @Override
    public void onGenericEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {

            System.out.println("バックアップ開始します。");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

            String base = "./backup/" + sdf.format(new Date());
            File file = new File(base);

            if (!new File("./backup").exists()){
                new File("./backup").mkdir();
            }

            if (!file.exists()){
                file.mkdir();
            }

            JDA jda = event.getJDA();
            List<Guild> guilds = jda.getGuilds();

            for (Guild guild : guilds){
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                String name = guild.getId()+guild.getName();
                System.out.println(name+"のバックアップ処理開始");
                File file1 = new File(base+"/"+name);
                if (!file1.exists()){
                    file1.mkdir();
                }

                List<TextChannel> channels = guild.getTextChannels();

                for (TextChannel channel : channels){
                    new Thread(()->{
                        File file2 = new File(base + "/" + name + "/" + channel.getName() + "-log.txt");
                        StringBuffer sb = new StringBuffer();
                        String lastId = "1";

                        try {
                            while (!lastId.equals("-1")){
                                String[] tempId = {"1"};
                                String finalLastId = lastId;

                                channel.getHistoryAfter(lastId, 100).queue(t -> {
                                    List<Message> history = t.getRetrievedHistory();
                                    if (history.size() > 0){
                                        tempId[0] = history.get(history.size() - 1).getId();
                                    } else {
                                        tempId[0] = "-1";
                                    }
                                    //System.out.println(channel.getName() + "の"+ finalLastId +"から取得");

                                    for (Message message : history){
                                        sb.append("-----------------------------------------------------------\n");
                                        sb.append("URL : ");
                                        sb.append(message.getJumpUrl());
                                        sb.append("\n");

                                        sb.append("MessageID : ");
                                        sb.append(message.getId());
                                        sb.append("\n");

                                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        sb.append("投稿日付 : ");
                                        sb.append(sf.format(Date.from(message.getTimeCreated().toInstant())));
                                        sb.append("\n");

                                        if (message.isEdited()){
                                            sb.append("編集日付 : ");
                                            sb.append(sf.format(Date.from(message.getTimeEdited().toInstant())));
                                            sb.append("\n");
                                        }

                                        sb.append("投稿者 : ");
                                        if (message.getMember() != null){
                                            if (message.getMember().getNickname() != null){
                                                sb.append(message.getMember().getNickname());
                                                sb.append(" (");
                                                sb.append(message.getMember().getUser().getAsTag());
                                                sb.append(")\n");
                                            } else {
                                                sb.append(message.getMember().getUser().getAsTag());
                                                sb.append("\n");
                                            }
                                            sb.append("Discord ID : ");
                                            sb.append(message.getMember().getId());
                                            sb.append("\n");
                                        } else {
                                            sb.append("削除済みユーザー？\n");
                                        }

                                        sb.append("---- 内容 start ----\n");
                                        sb.append(message.getContentRaw());
                                        sb.append("\n---- 内容 end----");

                                        if (message.getEmbeds().size() > 0){
                                            sb.append("\n---- 埋め込み start ----");
                                            for (MessageEmbed embed : message.getEmbeds()){
                                                sb.append("タイトル : ");
                                                sb.append(embed.getTitle());
                                                sb.append("\n");
                                                sb.append("内容:\n");
                                                sb.append(embed.getDescription());
                                                sb.append("\n");
                                                for (MessageEmbed.Field field : embed.getFields()) {
                                                    sb.append("---\nフィールド名 : ");
                                                    sb.append(field.getName());
                                                    sb.append("\n");
                                                    sb.append("内容 : \n");
                                                    sb.append(field.getValue());
                                                    sb.append("---\n");
                                                }

                                                sb.append("画像URL : ");
                                                if (embed.getImage() != null){
                                                    sb.append(embed.getImage().getProxyUrl());
                                                }
                                                sb.append("\n");

                                                sb.append("サムネイル画像URL : ");
                                                if (embed.getThumbnail() != null){
                                                    sb.append(embed.getThumbnail().getProxyUrl());
                                                }
                                                sb.append("\n");

                                                sb.append("フッター : ");
                                                sb.append(embed.getFooter());
                                            }

                                            sb.append("\n---- 埋め込み end ----\n");
                                        }

                                        if (message.getAttachments().size() > 0){
                                            sb.append("\n---- ファイル start ----\n");
                                            for (Message.Attachment attachment : message.getAttachments()) {
                                                sb.append("ファイル名 : ");
                                                sb.append(attachment.getFileName());
                                                sb.append("\n");
                                                sb.append("URL : ");
                                                sb.append(attachment.getProxyUrl());
                                                sb.append("\n");

                                                new Thread(()->{
                                                    try {
                                                        Thread.sleep(500);
                                                    } catch (InterruptedException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                    OkHttpClient client = new OkHttpClient();

                                                    Request request = new Request.Builder()
                                                            .url(attachment.getProxyUrl())
                                                            .build();
                                                    try (Response response = client.newCall(request).execute()) {
                                                        byte[] bytes = response.body().bytes();
                                                        if (!new File(base + "/" + name + "/" + channel.getName()).exists()) {
                                                            new File(base + "/" + name + "/" + channel.getName()).mkdir();
                                                        }

                                                        if (!new File(base + "/" + name + "/" + channel.getName() + "/" + message.getId()).exists()) {
                                                            new File(base + "/" + name + "/" + channel.getName() + "/" + message.getId()).mkdir();
                                                        }

                                                        try (FileOutputStream fos = new FileOutputStream(base + "/" + name + "/" + channel.getName() + "/" + message.getId() + "/" + attachment.getFileName())) {
                                                            fos.write(bytes);
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }).start();
                                            }
                                            sb.append("\n---- ファイル end ----\n");
                                        }

                                        sb.append("-----------------------------------------------------------");
                                    }
                                });
                                while (tempId[0].equals("1")){
                                    Thread.sleep(1000);
                                }

                                lastId = tempId[0];
                            }

                        } catch (Exception e){
                            e.printStackTrace();
                            return;
                        }

                        try {
                            if (!file2.exists()){
                                file2.createNewFile();
                            }

                            FileWriter filewriter = new FileWriter(file2);
                            filewriter.write(sb.toString());
                            filewriter.flush();
                            filewriter.close();
                        } catch (IOException e){
                            e.printStackTrace();
                            return;
                        }

                        try {
                            if (!file2.exists()){
                                file2.createNewFile();
                            }

                            FileWriter filewriter = new FileWriter(file2);
                            filewriter.write(sb.toString());
                            filewriter.flush();
                            filewriter.close();
                        } catch (IOException e){
                            e.printStackTrace();
                            return;
                        }
                    }).start();
                }

                File file2 = new File(base + "/" + name);

                if (!file2.exists()){
                    file2.mkdir();
                }

                File file3 = new File(base + "/" + name + "/ロールリスト.txt");
                StringBuffer buffer = new StringBuffer();
                for (Role role : guild.getRoles()) {
                    buffer.append(role.getName());
                    buffer.append("\n");
                }

                try {
                    if (!file3.exists()){
                        file3.createNewFile();
                    }

                    FileWriter filewriter = new FileWriter(file3);
                    filewriter.write(buffer.toString());
                    filewriter.flush();
                    filewriter.close();
                } catch (IOException e){
                    e.printStackTrace();
                    return;
                }


            }

            jda.shutdownNow();
        }
    }
}
