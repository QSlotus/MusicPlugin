package top.qiusyan;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class MusicPlugin extends JavaPlugin {

    private Gson gson;

    @Override
    public void onEnable() {
        getLogger().info("Music Plugin Enabled!");
        gson = new Gson(); // 初始化 Gson 实例
    }

    @Override
    public void onDisable() {
        getLogger().info("Music Plugin Disabled!");
    }

    // 处理搜索命令
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("music")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("search")) {
                String songName = args[1];
                searchSong(sender, songName);
                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("play")) {
                String songId = args[1];
                playSong(sender, songId);
                return true;
            }
        }
        return false;
    }

    // 发送歌曲查询请求
    private void searchSong(CommandSender sender, String songName) {
        try {
            URL url = new URL("https://netease.qiusyan.top/search?keywords=" + songName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            // 使用 Gson 解析 JSON 响应
            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
            JsonObject result = jsonResponse.getAsJsonObject("result");

            // 解析歌曲数据并显示
            JsonArray songs = result.getAsJsonArray("songs");
            for (int i = 0; i < songs.size(); i++) {
                JsonObject songObj = songs.get(i).getAsJsonObject();
                String songId = songObj.get("id").getAsString();
                String name = songObj.get("name").getAsString(); // 修改变量名避免重复
                String artistName = songObj.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();

                // 创建文本组件，包含点击事件
                TextComponent songText = new TextComponent(name + " - " + artistName + " - ");
                TextComponent playText = new TextComponent("[播放]");
                playText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/music play " + songId));
                playText.setColor(net.md_5.bungee.api.ChatColor.GREEN); // 设置播放按钮的颜色

                songText.addExtra(playText); // 将播放按钮附加到歌曲名称后

                // 发送消息给玩家
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.spigot().sendMessage(songText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("歌曲搜索失败，请重试！");
        }
    }

    // 播放歌曲
    private void playSong(CommandSender sender, String songId) {
        // 获取播放链接
        try {
            URL url = new URL("https://v.iarc.top/?type=url&id=" + songId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            String mp3Url = response.toString();

            // 这里你可以将MP3链接发送给玩家，或直接播放
            sender.sendMessage("正在播放歌曲：" + mp3Url);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("播放失败，请重试！");
        }
    }
}
