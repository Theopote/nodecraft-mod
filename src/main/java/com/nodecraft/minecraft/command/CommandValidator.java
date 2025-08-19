package com.nodecraft.minecraft.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.nodecraft.minecraft.registry.BlockRegistry;
import com.nodecraft.minecraft.registry.ItemRegistry;

/**
 * Minecraft命令验证器
 */
public class CommandValidator {
    private static CommandValidator instance;
    
    private final Map<String, CommandDefinition> commands = new HashMap<>();
    private final Pattern validCommandPattern = Pattern.compile("^/[a-zA-Z][a-zA-Z0-9_\\-]*($| .*)");
    
    // 单例模式
    private CommandValidator() {
        initializeCommands();
    }
    
    /**
     * 获取单例实例
     * @return 命令验证器实例
     */
    public static synchronized CommandValidator getInstance() {
        if (instance == null) {
            instance = new CommandValidator();
        }
        return instance;
    }
    
    /**
     * 验证命令是否有效
     * @param command 命令字符串
     * @return 是否有效
     */
    public boolean validateCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        
        // 命令必须以/开头
        if (!command.startsWith("/")) {
            return false;
        }
        
        // 命令格式必须匹配模式
        if (!validCommandPattern.matcher(command).matches()) {
            return false;
        }
        
        // 提取命令名称和参数
        String[] parts = command.substring(1).split(" ", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        // 查找命令定义
        CommandDefinition definition = commands.get(commandName);
        if (definition == null) {
            // 未知命令
            return false;
        }
        
        // 如果没有参数，检查命令是否允许无参数
        if (args.isEmpty()) {
            return definition.allowsNoParams;
        }
        
        // 使用规则验证参数
        return definition.validateArgs(args);
    }
    
    /**
     * 获取命令的错误消息
     * @param command 命令字符串
     * @return 错误消息，如果命令有效则返回null
     */
    public String getErrorMessage(String command) {
        if (command == null || command.isEmpty()) {
            return "命令不能为空";
        }
        
        if (!command.startsWith("/")) {
            return "命令必须以/开头";
        }
        
        if (!validCommandPattern.matcher(command).matches()) {
            return "命令格式无效";
        }
        
        String[] parts = command.substring(1).split(" ", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        CommandDefinition definition = commands.get(commandName);
        if (definition == null) {
            return "未知命令: " + commandName;
        }
        
        if (args.isEmpty() && !definition.allowsNoParams) {
            return "此命令需要参数";
        }
        
        if (!args.isEmpty() && !definition.validateArgs(args)) {
            String errorMessage = "参数无效: " + args;
            
            // 提供更具体的错误消息
            String[] argParts = args.trim().split(" ");
            for (int i = 0; i < Math.min(argParts.length, definition.params.size()); i++) {
                CommandParam param = definition.params.get(i);
                
                // 检查是否是方块ID或物品ID错误
                if (param.name.equals("block") && !Pattern.matches(param.pattern, argParts[i])) {
                    errorMessage = "无效的方块ID格式: " + argParts[i];
                    break;
                } else if (param.name.equals("block") && !definition.validateBlockId(argParts[i])) {
                    errorMessage = "未知的方块ID: " + argParts[i];
                    break;
                } else if (param.name.equals("item") && !Pattern.matches(param.pattern, argParts[i])) {
                    errorMessage = "无效的物品ID格式: " + argParts[i];
                    break;
                } else if (param.name.equals("item") && !definition.validateItemId(argParts[i])) {
                    errorMessage = "未知的物品ID: " + argParts[i];
                    break;
                } else if (!Pattern.matches(param.pattern, argParts[i])) {
                    errorMessage = "参数 '" + param.name + "' 格式无效: " + argParts[i];
                    break;
                }
            }
            
            return errorMessage;
        }
        
        return null; // 命令有效
    }
    
    /**
     * 获取所有命令名称
     * @return 命令名称列表
     */
    public List<String> getCommandNames() {
        return new ArrayList<>(commands.keySet());
    }
    
    /**
     * 获取命令定义
     * @param commandName 命令名称
     * @return 命令定义
     */
    public CommandDefinition getCommand(String commandName) {
        return commands.get(commandName.toLowerCase());
    }
    
    /**
     * 从命令初始部分获取可能的自动完成建议
     * @param partial 部分命令
     * @return 建议列表
     */
    public List<String> getSuggestions(String partial) {
        List<String> suggestions = new ArrayList<>();
        
        if (partial == null || partial.isEmpty()) {
            // 返回所有命令
            for (String cmd : commands.keySet()) {
                suggestions.add("/" + cmd);
            }
            return suggestions;
        }
        
        // 如果不以/开头，添加/
        if (!partial.startsWith("/")) {
            partial = "/" + partial;
        }
        
        // 提取命令部分
        String[] parts = partial.substring(1).split(" ", 2);
        String commandPart = parts[0].toLowerCase();
        
        if (parts.length == 1) {
            // 只有命令部分，列出匹配的命令
            for (String cmd : commands.keySet()) {
                if (cmd.startsWith(commandPart)) {
                    suggestions.add("/" + cmd);
                }
            }
        } else {
            // 有命令和参数部分，获取命令的参数建议
            CommandDefinition definition = commands.get(commandPart);
            if (definition != null) {
                String argPart = parts[1];
                List<String> argSuggestions = definition.getSuggestions(argPart);
                for (String suggestion : argSuggestions) {
                    suggestions.add("/" + commandPart + " " + suggestion);
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * 初始化命令定义
     */
    private void initializeCommands() {
        // 基础命令
        commands.put("help", new CommandDefinition("help", "显示帮助信息", true)
            .addParam("page", "页码", "\\d+", false)
            .addParam("command", "命令名称", "[a-zA-Z][a-zA-Z0-9_\\-]*", false));
            
        commands.put("give", new CommandDefinition("give", "给予物品", false)
            .addParam("player", "玩家名称", "[a-zA-Z0-9_]{3,16}", true)
            .addParam("item", "物品ID", "[a-z0-9_\\-:.]+", true)
            .addParam("amount", "数量", "\\d+", false)
            .addParam("data", "数据值", "\\d+", false)
            .addParam("nbt", "NBT数据", "\\{.*\\}", false));
            
        commands.put("teleport", new CommandDefinition("teleport", "传送实体", false)
            .addParam("target", "目标实体", "[a-zA-Z0-9_@\\[\\]\\{\\}=,]+", true)
            .addParam("location", "目标位置", "([~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d*)|([a-zA-Z0-9_]{3,16})", true)
            .addParam("rotation", "旋转角度", "[~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d*", false));
            
        commands.put("tp", commands.get("teleport")); // 别名
        
        commands.put("summon", new CommandDefinition("summon", "召唤实体", false)
            .addParam("entity", "实体类型", "[a-z0-9_\\-:.]+", true)
            .addParam("pos", "位置", "[~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d*", false)
            .addParam("nbt", "NBT数据", "\\{.*\\}", false));
            
        commands.put("setblock", new CommandDefinition("setblock", "设置方块", false)
            .addParam("pos", "位置", "[~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d*", true)
            .addParam("block", "方块ID", "[a-z0-9_\\-:.]+", true)
            .addParam("state", "方块状态", "\\[[a-z0-9_\\-:=,]+\\]", false)
            .addParam("mode", "模式", "(destroy|keep|replace)", false));
            
        commands.put("fill", new CommandDefinition("fill", "填充区域", false)
            .addParam("from", "起始位置", "[~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d*", true)
            .addParam("to", "结束位置", "[~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d* [~^]-?\\d*\\.?\\d*", true)
            .addParam("block", "方块ID", "[a-z0-9_\\-:.]+", true)
            .addParam("mode", "模式", "(destroy|hollow|keep|outline|replace)", false));
            
        commands.put("clear", new CommandDefinition("clear", "清空背包", true)
            .addParam("target", "目标实体", "[a-zA-Z0-9_@\\[\\]\\{\\}=,]+", false)
            .addParam("item", "物品ID", "[a-z0-9_\\-:.]+", false)
            .addParam("maxCount", "最大数量", "\\d+", false));
            
        commands.put("weather", new CommandDefinition("weather", "更改天气", false)
            .addParam("type", "天气类型", "(clear|rain|thunder)", true)
            .addParam("duration", "持续时间", "\\d+", false));
            
        commands.put("time", new CommandDefinition("time", "设置时间", false)
            .addParam("action", "操作", "(add|query|set)", true)
            .addParam("value", "值", "(day|night|noon|midnight|\\d+)", false));
            
        // 添加搜索命令
        commands.put("search", new CommandDefinition("search", "搜索游戏内容", false)
            .addParam("type", "类型", "(block|item)", true)
            .addParam("keyword", "关键词", "[a-z0-9_\\-:.]+", true)
            .addParam("limit", "结果数量限制", "\\d+", false));
    }
    
    /**
     * 命令定义类
     */
    public static class CommandDefinition {
        private final String name;
        private final String description;
        private final boolean allowsNoParams;
        private final List<CommandParam> params = new ArrayList<>();
        
        public CommandDefinition(String name, String description, boolean allowsNoParams) {
            this.name = name;
            this.description = description;
            this.allowsNoParams = allowsNoParams;
        }
        
        public CommandDefinition addParam(String name, String description, String pattern, boolean required) {
            params.add(new CommandParam(name, description, pattern, required));
            return this;
        }
        
        /**
         * 验证命令参数
         * @param args 参数字符串
         * @return 是否有效
         */
        public boolean validateArgs(String args) {
            // 简单验证：确保所有必需参数都有值
            String[] argParts = args.trim().split(" ");
            
            int requiredCount = 0;
            for (CommandParam param : params) {
                if (param.required) {
                    requiredCount++;
                }
            }
            
            // 如果提供的参数少于必需参数，则无效
            if (argParts.length < requiredCount) {
                return false;
            }
            
            // 对每个参数进行模式匹配验证
            for (int i = 0; i < Math.min(argParts.length, params.size()); i++) {
                CommandParam param = params.get(i);
                if (!Pattern.matches(param.pattern, argParts[i])) {
                    return false;
                }
                
                // 对方块和物品ID进行额外验证
                if (param.name.equals("block") && !validateBlockId(argParts[i])) {
                    return false;
                } else if (param.name.equals("item") && !validateItemId(argParts[i])) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * 验证方块ID是否存在
         * @param blockId 方块ID
         * @return 是否存在
         */
        private boolean validateBlockId(String blockId) {
            // 如果ID以minecraft:开头或包含:字符，尝试在注册表中查找
            if (blockId.contains(":")) {
                return BlockRegistry.getInstance().containsKey(blockId);
            }
            // 如果用户输入的是没有命名空间的ID，假设是minecraft命名空间
            return BlockRegistry.getInstance().containsKey("minecraft:" + blockId);
        }
        
        /**
         * 验证物品ID是否存在
         * @param itemId 物品ID
         * @return 是否存在
         */
        private boolean validateItemId(String itemId) {
            // 如果ID以minecraft:开头或包含:字符，尝试在注册表中查找
            if (itemId.contains(":")) {
                return ItemRegistry.getInstance().containsKey(itemId);
            }
            // 如果用户输入的是没有命名空间的ID，假设是minecraft命名空间
            return ItemRegistry.getInstance().containsKey("minecraft:" + itemId);
        }
        
        /**
         * 获取参数自动完成建议
         * @param partial 部分参数
         * @return 建议列表
         */
        public List<String> getSuggestions(String partial) {
            List<String> suggestions = new ArrayList<>();
            String[] argParts = partial.trim().split(" ");
            
            // 确定当前正在输入的参数索引
            int currentParamIndex = argParts.length - 1;
            
            // 如果索引超出参数定义范围，使用最后一个参数
            if (currentParamIndex >= params.size()) {
                currentParamIndex = params.size() - 1;
            }
            
            // 如果没有参数定义，返回空列表
            if (params.isEmpty() || currentParamIndex < 0) {
                return suggestions;
            }
            
            // 获取当前参数的示例值（这里可以扩展为从游戏状态获取更具体的建议）
            CommandParam currentParam = params.get(currentParamIndex);
            
            // 添加一些基于当前参数类型的示例值
            if (currentParam.name.equals("player")) {
                suggestions.addAll(Arrays.asList("Player1", "Player2", "@p", "@a", "@r"));
            } else if (currentParam.name.equals("block") || currentParam.name.equals("item")) {
                // 集成BlockRegistry和ItemRegistry来获取实际的方块和物品ID
                if (currentParam.name.equals("block")) {
                    // 获取方块ID
                    List<String> blockIds = BlockRegistry.getInstance().getBlockIds();
                    if (!blockIds.isEmpty()) {
                        String currentInput = argParts[currentParamIndex];
                        List<String> filteredBlocks = new ArrayList<>();
                        
                        // 根据用户输入过滤方块ID
                        for (String blockId : blockIds) {
                            if (blockId.contains(currentInput)) {
                                filteredBlocks.add(blockId);
                                // 限制最多10个建议
                                if (filteredBlocks.size() >= 10) {
                                    break;
                                }
                            }
                        }
                        
                        // 如果没有匹配项，则添加前10个方块
                        if (filteredBlocks.isEmpty()) {
                            suggestions.addAll(blockIds.subList(0, Math.min(10, blockIds.size())));
                        } else {
                            suggestions.addAll(filteredBlocks);
                        }
                    } else {
                        suggestions.addAll(Arrays.asList("minecraft:stone", "minecraft:dirt", "minecraft:oak_log"));
                    }
                } else {
                    // 获取物品ID
                    List<String> itemIds = ItemRegistry.getInstance().getItemIds();
                    if (!itemIds.isEmpty()) {
                        String currentInput = argParts[currentParamIndex];
                        List<String> filteredItems = new ArrayList<>();
                        
                        // 根据用户输入过滤物品ID
                        for (String itemId : itemIds) {
                            if (itemId.contains(currentInput)) {
                                filteredItems.add(itemId);
                                // 限制最多10个建议
                                if (filteredItems.size() >= 10) {
                                    break;
                                }
                            }
                        }
                        
                        // 如果没有匹配项，则添加前10个物品
                        if (filteredItems.isEmpty()) {
                            suggestions.addAll(itemIds.subList(0, Math.min(10, itemIds.size())));
                        } else {
                            suggestions.addAll(filteredItems);
                        }
                    } else {
                        suggestions.addAll(Arrays.asList("minecraft:apple", "minecraft:diamond", "minecraft:iron_ingot"));
                    }
                }
            } else if (currentParam.name.equals("pos") || currentParam.name.equals("location")) {
                suggestions.addAll(Arrays.asList("~ ~ ~", "~1 ~2 ~3", "0 64 0"));
            } else if (currentParam.name.equals("entity")) {
                suggestions.addAll(Arrays.asList("minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"));
            } else {
                // 为其他类型添加一些基本建议
                suggestions.add(currentParam.name + " (" + currentParam.description + ")");
            }
            
            // 过滤建议，只保留与部分输入匹配的建议
            String currentInput = argParts[currentParamIndex];
            List<String> filteredSuggestions = new ArrayList<>();
            
            for (String suggestion : suggestions) {
                if (suggestion.startsWith(currentInput)) {
                    // 构建完整建议，保留先前的参数
                    StringBuilder fullSuggestion = new StringBuilder();
                    for (int i = 0; i < currentParamIndex; i++) {
                        fullSuggestion.append(argParts[i]).append(" ");
                    }
                    fullSuggestion.append(suggestion);
                    
                    filteredSuggestions.add(fullSuggestion.toString());
                }
            }
            
            return filteredSuggestions.isEmpty() ? suggestions : filteredSuggestions;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<CommandParam> getParams() {
            return new ArrayList<>(params);
        }
        
        public boolean isAllowsNoParams() {
            return allowsNoParams;
        }
    }
    
    /**
     * 命令参数定义
     */
    public static class CommandParam {
        private final String name;
        private final String description;
        private final String pattern;
        private final boolean required;
        
        public CommandParam(String name, String description, String pattern, boolean required) {
            this.name = name;
            this.description = description;
            this.pattern = pattern;
            this.required = required;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
    }
} 