package com.muffinhead.MRPGNPC;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.mob.EntityZombie;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.muffinhead.MRPGNPC.Events.MobNPCBeAttack;
import com.muffinhead.MRPGNPC.NPCs.MobNPC;
import com.muffinhead.MRPGNPC.NPCs.NPC;
import com.muffinhead.MRPGNPC.Tasks.AutoSpawn;
import com.muffinhead.MRPGNPC.Tasks.worldRandomSpawn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.nukkit.utils.Utils.readFile;

public class MRPGNPC extends PluginBase {
    public static MRPGNPC mrpgnpc;
    //public static Map<String, CompoundTag> tagMap = new HashMap<>();
    public static ConcurrentHashMap<String, Config> mobconfigs = new ConcurrentHashMap<String, Config>();
    public static ConcurrentHashMap<String, Config> pointconfigs = new ConcurrentHashMap<String, Config>();
    public static ConcurrentHashMap<String, Config> skillconfigs = new ConcurrentHashMap<String, Config>();
    public static Map<String, CompoundTag> skinTags = new HashMap<>();
    public static ConcurrentHashMap<String, Skin> skins = new ConcurrentHashMap<>();
    public static Config worldSpawnConfig;




    @Override
    public void onLoad() {
        Entity.registerEntity("MobNPC",MobNPC.class);
    }

    @Override
    public void onEnable() {
        getServer().getLogger().info("MRPGNPC1.0.0(PNX Version) is enable!The author is Reiyans and MuffinHead");
        getServer().getLogger().info("MRPGNPC1.0.0(PNX版本) 启动成功!插件作者是 Reiyans 和 MuffinHead");
        getServer().getPluginManager().registerEvents(new MobNPCBeAttack(),this);
        mrpgnpc = this;
        checkMobs();
        checkPoints();
        checkSkills();
        checkWorldSpawnConfig();
        getServer().getScheduler().scheduleDelayedRepeatingTask(new AutoSpawn(),1,1);
        getServer().getScheduler().scheduleDelayedRepeatingTask(new worldRandomSpawn(),20,20);
        try {
            checkSkins();
        } catch (IOException e) {
            getServer().getLogger().alert("Skins files check wrong！！");
            getServer().getLogger().alert("检查到皮肤文件出错！！");
        }
    }


    @Override
    public void onDisable() {
        for (Level level :getServer().getLevels().values()){
            for (Entity entity:level.getEntities()){
                if (entity instanceof MobNPC){
                    entity.kill();
                }
            }
        }
    }

    //command part
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mrn") && command.getName().equalsIgnoreCase("刷怪")) {
            if (args.length <= 0) return false;
            switch (args[0]) {
                case "clear":{
                    if (args.length <= 1) return false;
                    switch (args[1]) {
                        case "mobs":{
                            for (Level level:getServer().getLevels().values()){
                                for (Entity entity:level.getEntities()){
                                    if (entity instanceof MobNPC) {
                                        entity.kill();
                                    }
                                }
                            }
                        }
                        case "drops": {
                            for (Level level:getServer().getLevels().values()){
                                for (Entity entity:level.getEntities()){
                                    if (entity instanceof EntityItem) {
                                        entity.kill();
                                    }
                                }
                            }
                        }
                    }
                }
                case "清除":{
                    if (args.length <= 1) return false;
                    switch (args[1]) {
                        case "怪物":{
                            for (Level level:getServer().getLevels().values()){
                                for (Entity entity:level.getEntities()){
                                    if (entity instanceof MobNPC) {
                                        entity.kill();
                                    }
                                }
                            }
                        }
                        case "掉落物": {
                            for (Level level:getServer().getLevels().values()){
                                for (Entity entity:level.getEntities()){
                                    if (entity instanceof EntityItem) {
                                        entity.kill();
                                    }
                                }
                            }
                        }
                    }
                }
                case "mob": {
                    if (args.length <= 1) return false;
                    switch (args[1]) {
                        case "create": {
                            if (args.length >= 3) {
                                File mobFile = getMobFolder().resolve(args[2] + ".yml").toFile();
                                if (mobFile.exists()) {
                                    sender.sendMessage("§cThis mobfile is already exist!");
                                    return true;
                                }
                                Config config = createMobConfig(mobFile.getPath());
                                config.save();
                                sender.sendMessage("§aThe new mobfile was created successfully!");
                                return true;
                            }
                        }
                        case "delete": {
                            if (args.length >= 3) {
                                File mobFile = getMobFolder().resolve(args[2] + ".yml").toFile();
                                if (!mobFile.exists()) {
                                    sender.sendMessage("§cCan't find the file");

                                } else {
                                    mobFile.deleteOnExit();
                                    sender.sendMessage("§aThe mobfile was deleted successfully!");
                                }
                                return true;
                            }
                        }
                        case "spawn": {
                            NPC npc = spawnNPC(sender, args);
                            if (npc!=null){
                                npc.spawnToAll();
                            }
                            return true;
                        }
                    }
                    return false;
                }
                case "怪物": {
                    if (args.length <= 1) return false;
                    switch (args[1]) {
                        case "创建": {
                            if (args.length >= 3) {
                                File mobFile = getMobFolder().resolve(args[2] + ".yml").toFile();
                                if (mobFile.exists()) {
                                    sender.sendMessage("§c这个名字的怪物文件已经存在!");
                                    return true;
                                }
                                Config config = createMobConfig(mobFile.getPath());
                                config.save();
                                sender.sendMessage("§a新的怪物配置文件创建成功!");
                                return true;
                            }
                        }
                        case "删除": {
                            if (args.length >= 3) {
                                File mobFile = getMobFolder().resolve(args[2] + ".yml").toFile();
                                if (!mobFile.exists()) {
                                    sender.sendMessage("§c没有找到要删除的配置文件");

                                } else {
                                    mobFile.deleteOnExit();
                                    sender.sendMessage("§a这个怪物的配置文件已经删除!");
                                }
                                return true;
                            }
                        }
                        case "生成": {
                            NPC npc = spawnNPC(sender, args);
                            if (npc!=null){
                                npc.spawnToAll();
                            }
                            return true;
                        }
                    }
                    return false;
                }
                case "test": {
                    if (sender instanceof Player) {
                        Location spawnLocation = ((Player) sender).getLocation();
                        String mobfile = "测试A号";
                        String mobFeature = "MDungeon" + ":" + mobfile + ":" + "" + ":" + 0;
                        MobNPC npc = MRPGNPC.mrpgnpc.spawnNPC(Server.getInstance().getConsoleSender(), mobfile, spawnLocation, mobFeature);
                        npc.getActiveattackcreature().add("MDungeon");
                        npc.getUnattractivecreature().add("NotMDungeon");
                        npc.spawnToAll();
                        return true;
                    }
                }
                case "point": {
                    if (args.length <= 1) return false;
                    switch (args[1]) {
                        case "create": {
                            if (sender instanceof Player) {
                                if (args.length >= 3) {
                                    File pointFile = getPointFolder().resolve(args[2] + ".yml").toFile();
                                    if (pointFile.exists()) {
                                        sender.sendMessage("§cThis pointfile is already exist!");
                                        return true;
                                    }
                                    Config config = createPointConfig(pointFile.getPath(), ((Player) sender));
                                    config.save();
                                    sender.sendMessage("§aThe new pointfile was created successfully!");
                                    return true;
                                }
                            }
                        }
                        case "delete": {
                            if (args.length >= 3) {
                                File pointFile = getPointFolder().resolve(args[2] + ".yml").toFile();
                                if (!pointFile.exists()) {
                                    sender.sendMessage("§cCan't find the file");
                                } else {
                                    pointFile.deleteOnExit();
                                    sender.sendMessage("§aThe pointfile was deleted successfully!");
                                }
                                return true;
                            }
                        }
                    }
                }
                case "刷怪点": {
                    if (args.length <= 1) return false;
                    switch (args[1]) {
                        case "创建": {
                            if (sender instanceof Player) {
                                if (args.length >= 3) {
                                    File pointFile = getPointFolder().resolve(args[2] + ".yml").toFile();
                                    if (pointFile.exists()) {
                                        sender.sendMessage("§c这个名字的刷怪点配置文件已经存在!");
                                        return true;
                                    }
                                    Config config = createPointConfig(pointFile.getPath(), ((Player) sender));
                                    config.save();
                                    sender.sendMessage("§a新的刷怪点配置文件创建成功!");
                                    return true;
                                }
                            }
                        }
                        case "删除": {
                            if (args.length >= 3) {
                                File pointFile = getPointFolder().resolve(args[2] + ".yml").toFile();
                                if (!pointFile.exists()) {
                                    sender.sendMessage("§c没有找到这个刷怪点的配置文件");
                                } else {
                                    pointFile.deleteOnExit();
                                    sender.sendMessage("§a这个刷怪点配置文件删除成功!");
                                }
                                return true;
                            }
                        }
                    }
                }
                case "reload":{
                    checkMobs();
                    checkPoints();
                    checkSkills();
                    for (Level level:getServer().getLevels().values()){
                        for (Entity entity:level.getEntities()){
                            if (!(entity instanceof Player)){
                                entity.close();
                            }
                        }
                    }
                    //getServer().getScheduler().scheduleDelayedRepeatingTask(new AutoSpawn(),1,1);
                    try {
                        checkSkins();
                    } catch (IOException e) {
                        getServer().getLogger().alert("Skins check wrong！！");
                    }
                    sender.sendMessage("§aThe mob&point files was reload successfully!");
                    return true;
                }
                case "重载":{
                    checkMobs();
                    checkPoints();
                    checkSkills();
                    for (Level level:getServer().getLevels().values()){
                        for (Entity entity:level.getEntities()){
                            if (!(entity instanceof Player)){
                                entity.close();
                            }
                        }
                    }
                    //getServer().getScheduler().scheduleDelayedRepeatingTask(new AutoSpawn(),1,1);
                    try {
                        checkSkins();
                    } catch (IOException e) {
                        getServer().getLogger().alert("皮肤文件出错！！");
                    }
                    sender.sendMessage("§a怪物与刷怪点重新加载成功!");
                    return true;
                }

                //put on the back burner
                case "skill": {
                    switch (args[1]){
                        case "create":{
                            if (args.length >= 3) {
                                File skillFile = getSkillFolder().resolve(args[2] + ".yml").toFile();
                                if (skillFile.exists()) {
                                    sender.sendMessage("§cThis skillfile is already exist!");
                                    return true;
                                }
                                Config config = createSkillConfig(skillFile.getPath());
                                config.save();
                                sender.sendMessage("§aThe new skillfile was created successfully!");
                                return true;
                            }
                        }
                    }
                }
                case "技能": {
                    switch (args[1]){
                        case "创建":{
                            if (args.length >= 3) {
                                File skillFile = getSkillFolder().resolve(args[2] + ".yml").toFile();
                                if (skillFile.exists()) {
                                    sender.sendMessage("§c这个名字的技能配置文件已经存在!");
                                    return true;
                                }
                                Config config = createSkillConfig(skillFile.getPath());
                                config.save();
                                sender.sendMessage("§a新的技能配置文件成功创建!");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return super.onCommand(sender, command, label, args);

    }
//command part


    public Path getMobFolder() {
        return getDataFolder().toPath().resolve("Mobs");
    }

    public Path getPointFolder() {
        return getDataFolder().toPath().resolve("Points");
    }

    public Path getSkillFolder() {
        return getDataFolder().toPath().resolve("Skills");
    }

    public Config createMobConfig(String configPath) {
        Config config = new Config(configPath, Config.YAML);
        config.set("DisplayName", "Mob");
        config.set("MaxHealth", 40);
        config.set("Size", 1.0);
        config.set("MovementSpeed", 1.0);
        config.set("Damage", 3.0);
        config.set("KnockBack", 0.1);
        config.set("DefenseFormula", "source.damage");
        config.set("AttackDelay", 30);
        config.set("DamageDelay", 0);
        config.set("BedamagedDelay", 0);
        config.set("AttackRange", 1.2);
        config.set("HitRange",0.15);
        config.set("HateRange", 15.0);
        config.set("HitRange",0.15);
        config.set("NoHatesHeal", "200:1.0");
        config.set("CanBeKnockBack", false);
        config.set("BoudningBox",true);
        config.set("DeathCommands", new ArrayList<>());
        config.set("Skin", "GreenCross");
        config.set("ItemInHand", "267:0");
        config.set("BeDamagedBlockParticleID", "152:0");
        config.set("MoveLimitDistance", 15.0);
        config.set("ActiveAttackCreature",new ArrayList<>());
        config.set("UnattractiveCreature",new ArrayList<>());
        config.set("Drops", new ArrayList<>());
        config.set("Camp", "Example");
        config.set("Skills",new ArrayList<>());
        return config;
    }

    /*
    mobfilename-respawntick-1timespawnamount-maxamount-spawnlimit
     */
    public Config createPointConfig(String configPath,Player player) {
        Config config = new Config(configPath, Config.YAML);
        config.set("PointName", "A");
        config.set("PointPosition",player.getX()+":"+player.getY()+":"+player.getZ()+":"+player.getLevel().getName()+":"+player.getYaw()+":"+player.getPitch());
        config.set("SpawnList", new ArrayList<>());
        return config;
    }

    public Config createSkillConfig(String configPath) {
        Config config = new Config(configPath, Config.YAML);
        config.set("Skills", new ArrayList<>());
        return config;
    }
    public MobNPC spawnNPC(CommandSender sender, String mobfile, Location location, String mobFeature) {
        String[] args = new String[9];
        args[0] = "";
        args[1] = "";
        args[2] = mobfile;
        args[3] = String.valueOf(location.x);
        args[4] = String.valueOf(location.y);
        args[5] = String.valueOf(location.z);
        args[6] = location.getLevel().getName();
        args[7] = String.valueOf(location.getYaw());
        args[8] = String.valueOf(location.getPitch());
        MobNPC npc = spawnNPC(sender,args);
        npc.setMobFeature(mobFeature);
        return npc;
    }

    public MobNPC spawnNPC(CommandSender sender, String[] args) {
        if (args.length < 7) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("The console need type the coordinates");
                return null;
            }else{
                Config config = mobconfigs.get(args[2]);
                if (config!=null) {
                    MobNPC npc = new MobNPC(((Player) sender).getChunk(), NPC.getDefaultNBT(((Player) sender).getTargetBlock(3)));
                    npc.setDisplayName(config.getString("DisplayName"));
                    npc.setMaxHealth(config.getInt("MaxHealth"));
                    npc.setHealth(npc.getMaxHealth());
                    npc.setScale((float) config.getDouble("Size"));
                    npc.setSpeed(config.getDouble("MovementSpeed"));
                    npc.setDamage(config.getDouble("Damage"));
                    npc.setKnockback(config.getDouble("Knockback"));
                    npc.setDefenseformula(config.getString("DefenseFormula"));
                    npc.setAttackdelay(config.getInt("AttackDelay"));
                    npc.setDamagedelay(config.getInt("DamageDelay"));
                    npc.setBedamageddelay(config.getInt("BedamagedDelay"));
                    npc.setAttackrange(config.getDouble("AttackRange"));
                    npc.setHaterange(config.getDouble("HateRange"));
                    npc.setNohatesheal(config.getString("NoHatesHeal"));
                    npc.setHitrange(config.getDouble("HitRange"));
                    npc.setCanbeknockback(config.getBoolean("CanBeKnockBack"));
                    npc.setDeathcommands(new ArrayList<>(config.getList("DeathCommands")));
                    npc.setCamp(config.getString("Camp"));
                    npc.setSkills(new ArrayList<>(config.getList("Skills")));
                    npc.getInventory().setItemInHand(getItemByString(config.getString("ItemInHand")));
                    npc.setBeDamagedblockparticle(config.getString("BeDamagedBlockParticleID"));
                    npc.setActiveattackcreature(new ArrayList<>(config.getList("ActiveAttackCreature")));
                    npc.setUnattractivecreature(new ArrayList<>(config.getList("UnattractiveCreature")));
                    npc.setDrops(new ArrayList<>(config.getList("Drops")));
                    npc.setSkinname(config.getString("Skin"));
                    npc.setSkin(skins.get(config.getString("Skin")));
                    npc.setEnableBox(config.getBoolean("BoundingBox"));
                    npc.moveLimitDistance = config.getDouble("MoveLimitDistance");
                    return npc;
                }
            }
        }else{
            Config config = mobconfigs.get(args[2]);
            if (config!=null) {
                Location location = new Location(Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]),Double.parseDouble(args[7]),Double.parseDouble(args[8]),getServer().getLevelByName(args[6]));
                MobNPC npc = new MobNPC(location.getChunk(), MobNPC.getDefaultNBT(location));
                npc.setDisplayName(config.getString("DisplayName"));
                npc.setMaxHealth(config.getInt("MaxHealth"));
                npc.setHealth(npc.getMaxHealth());
                npc.setScale((float) config.getDouble("Size"));
                npc.setSpeed(config.getDouble("MovementSpeed"));
                npc.setDamage(config.getDouble("Damage"));
                npc.setKnockback(config.getDouble("Knockback"));
                npc.setDefenseformula(config.getString("DefenseFormula"));
                npc.setAttackdelay(config.getInt("AttackDelay"));
                npc.setDamagedelay(config.getInt("DamageDelay"));
                npc.setBedamageddelay(config.getInt("BedamagedDelay"));
                npc.setAttackrange(config.getDouble("AttackRange"));
                npc.setHaterange(config.getDouble("HateRange"));
                npc.setNohatesheal(config.getString("NoHatesHeal"));
                npc.setHitrange(config.getDouble("HitRange"));
                npc.setCanbeknockback(config.getBoolean("CanBeKnockBack"));
                npc.setDeathcommands(new ArrayList<>(config.getList("DeathCommands")));
                npc.setCamp(config.getString("Camp"));
                npc.setSkills(new ArrayList<>(config.getList("Skills")));
                npc.getInventory().setItemInHand(getItemByString(config.getString("ItemInHand")));
                npc.setBeDamagedblockparticle(config.getString("BeDamagedBlockParticleID"));
                npc.setActiveattackcreature(new ArrayList<>(config.getList("ActiveAttackCreature")));
                npc.setUnattractivecreature(new ArrayList<>(config.getList("UnattractiveCreature")));
                npc.setDrops(new ArrayList<>(config.getList("Drops")));
                npc.setSkinname(config.getString("Skin"));
                npc.setSkin(skins.get(config.getString("Skin")));
                npc.setEnableBox(config.getBoolean("BoundingBox"));
                npc.moveLimitDistance = config.getDouble("MoveLimitDistance");
                return npc;
            }
        }
        return null;
    }


    public Item getItemByString(String s){
        Item item = Item.get(Integer.parseInt(s.split(":")[0]));
        item.setDamage(Integer.parseInt(s.split(":")[1]));
        return item;
    }
    public void checkSkins() throws IOException {
        Path skinPath = getDataFolder().toPath().resolve("Skins");
        File skinsFolder = new File(skinPath.toString());
        if (!skinsFolder.exists()) {
            skinsFolder.mkdirs();
        }
        for (File skinFolder : Objects.requireNonNull(skinsFolder.listFiles())) {


            File geometry = skinFolder.toPath().resolve("geometry.json").toFile();
            Config config = new Config(geometry.getPath());
            Skin skin = null;
            if (!config.getAll().containsKey("format_version")) {
                if (config.getString("format_version").equals("1.10.0")){
                    skin = newSkinNew(skinFolder.toPath());
                }else {
                    skin = newSkinOld(skinFolder.toPath());
                }
            }else{
                skin = newSkinNew(skinFolder.toPath());
            }

            Path capePath = skinFolder.toPath().resolve("cape.png");
            if (capePath.toFile().exists()) {
                try {
                    BufferedImage capeData;
                    capeData = ImageIO.read(capePath.toFile());
                    skin.setCapeData(capeData);
                    skin.setCapeId(skinFolder.getPath());
                } catch (IOException e) {
                    System.out.println("Cape" + skinFolder.getName() + "can't use");
                }
            }
            skins.put(skinFolder.getName(), skin);
        }
    }
    public Skin newSkinOld(Path path) throws IOException {
        Skin skin = new Skin();
        Path skinfolder = getDataFolder().toPath().resolve("Skins");
        Path skinthings = skinfolder.resolve(path);
        Path skinpath = skinthings.resolve("skin.png");
        Path geometrypath = skinthings.resolve("geometry.json");
        // Path animationpath = skinPath.resolve(skinthings.getName()).resolve("geometry.animation.json");
        BufferedImage skindata = null;
        String skingeometry = null;
        //  String skinanimation = null;
        try {
            skindata = ImageIO.read(skinpath.toFile());
            skingeometry = new String(Files.readAllBytes(geometrypath), StandardCharsets.UTF_8);
            //skinanimation = new String(Files.readAllBytes(animationpath),StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(skinpath + ":" + geometrypath);
        }
        if (skindata != null) {
            skin.setSkinData(skindata);
            // if (skinanimation!=null) {

            //      skin.setAnimationData(skinanimation);
            // }
            if (skingeometry != null) {
                skin.setGeometryData(skingeometry);
            } else {
                System.out.println("皮肤模型出错");
            }
            skin.setGeometryName("geometry.mrpgnpc");
            skin.setSkinId(skinthings.toFile().getName());
        }
        return skin;
    }


    public Skin newSkinNew(Path path) throws IOException {
        Skin skin = new Skin();
        skin.generateSkinId("mrpgnpc");
        skin.setGeometryName(path.toString()+"/geometry.mrpgnpc");
        skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"geometry.mrpgnpc\"}}");
        skin.setGeometryData(new String(Files.readAllBytes(Paths.get(path.toString() + "/geometry.json"))));
        skin.setSkinData(ImageIO.read(Paths.get(path.toString() + "/skin.png").toFile()));
        skin.setTrusted(true);
        return skin;
    }
    public void checkWorldSpawnConfig(){
        worldSpawnConfig = new Config(getDataFolder().toPath().resolve("worldRandomSpawn.yml").toString(), Config.YAML);
    }
    public void checkMobs(){
        File mobsFolder = getMobFolder().toFile();
        if (!mobsFolder.exists()) {
            mobsFolder.mkdirs();
        }
        for (File mobfile : Objects.requireNonNull(mobsFolder.listFiles())) {
            Config config = new Config(mobfile.getPath());
            mobconfigs.put(mobfile.getName().replace(".yml", ""), config);
        }
    }
    public void checkPoints(){
        File pointsFolder = getPointFolder().toFile();
        if (!pointsFolder.exists()) {
            pointsFolder.mkdirs();
        }
        for (File pointfile : Objects.requireNonNull(pointsFolder.listFiles())) {
            Config config = new Config(pointfile.getPath());
            pointconfigs.put(pointfile.getName().replace(".yml", ""), config);
        }
    }
    public void checkSkills(){
        File skillsFolder = getSkillFolder().toFile();
        if (!skillsFolder.exists()) {
            skillsFolder.mkdirs();
        }
        for (File skillfile : Objects.requireNonNull(skillsFolder.listFiles())) {
            Config config = new Config(skillfile.getPath());
            skillconfigs.put(skillfile.getName().replace(".yml", ""), config);
        }
    }
}
