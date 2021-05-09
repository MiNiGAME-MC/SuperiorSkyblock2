package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.ModulesManager;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public final class ModulesHandler extends AbstractHandler implements ModulesManager {

    private final static Registry<String, PluginModule> modulesMap = Registry.createRegistry();
    private final static Registry<PluginModule, ModuleData> modulesData = Registry.createRegistry();
    private final File modulesFolder;

    public ModulesHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
        modulesFolder = new File(plugin.getDataFolder(), "modules");
    }

    @Override
    public void loadData() {
        if(!modulesFolder.exists())
            //noinspection ResultOfMethodCallIgnored
            modulesFolder.mkdirs();
    }

    @Override
    public void registerModule(PluginModule pluginModule) {
        String moduleName = pluginModule.getName().toLowerCase();

        Preconditions.checkState(!modulesMap.containsKey(moduleName), "PluginModule with the name " + moduleName + " already exists.");

        File dataFolder = new File(modulesFolder, pluginModule.getName());

        pluginModule.initModule(dataFolder);

        try {
            long startTime = System.currentTimeMillis();

            SuperiorSkyblockPlugin.log("&aEnabling the module " + pluginModule.getName() + "...");

            pluginModule.onEnable(plugin);

            Listener[] listeners = pluginModule.getModuleListeners();
            SuperiorCommand[] commands = pluginModule.getSuperiorCommands();
            SuperiorCommand[] adminCommands = pluginModule.getSuperiorAdminCommands();

            if(listeners != null || commands != null || adminCommands != null)
                modulesData.add(pluginModule, new ModuleData(listeners, commands, adminCommands));

            if(listeners != null){
                for(Listener listener : listeners)
                    Bukkit.getPluginManager().registerEvents(listener, plugin);
            }

            if(commands != null){
                for(SuperiorCommand command : commands)
                    plugin.getCommands().registerCommand(command);
            }

            if(adminCommands != null){
                for(SuperiorCommand adminCommand : adminCommands)
                    plugin.getCommands().registerAdminCommand(adminCommand);
            }

            SuperiorSkyblockPlugin.log("&eFinished enabling the module " + pluginModule.getName() +
                    " (Took " + (System.currentTimeMillis() - startTime) + "ms)");

            modulesMap.add(moduleName, pluginModule);
        } catch (Exception ex){
            SuperiorSkyblockPlugin.log("&cAn error occurred while enabling the module " + pluginModule.getName() + ":");
            ex.printStackTrace();
            SuperiorSkyblockPlugin.log("&cContact " + pluginModule.getAuthor() + " regarding this, this has nothing to do with the plugin.");

            // Calling onDisable so the plugin can unregister its data if needed
            pluginModule.onDisable();
        }
    }

    @Override
    public void unregisterModule(PluginModule pluginModule) {
        String moduleName = pluginModule.getName().toLowerCase();

        Preconditions.checkState(modulesMap.containsKey(moduleName), "PluginModule with the name " + moduleName + " is not registered in the plugin anymore.");

        SuperiorSkyblockPlugin.log("&cDisabling the module " + pluginModule.getName() + "...");

        pluginModule.onDisable();

        ModuleData moduleData = modulesData.remove(pluginModule);

        if(moduleData != null){
            if(moduleData.listeners != null)
                Arrays.stream(moduleData.listeners).forEach(HandlerList::unregisterAll);
        }

        modulesMap.remove(moduleName);
    }

    @Override
    public PluginModule getModule(String name) {
        return modulesMap.get(name.toLowerCase());
    }

    @Override
    public Collection<PluginModule> getModules() {
        return modulesMap.values();
    }

    public void loadExternalModules(){
        File[] folderFiles = modulesFolder.listFiles();

        if(folderFiles != null) {
            for (File file : folderFiles) {
                if(!file.getName().endsWith(".jar"))
                    continue;

                String moduleName = file.getName().replace(".jar", "");

                try {
                    //noinspection deprecation
                    Optional<Class<?>> moduleClass = FileUtils.getClasses(file.toURL(), PluginModule.class).stream().findFirst();

                    if (!moduleClass.isPresent())
                        throw new NullPointerException("The module " + moduleName + " is not valid.");

                    PluginModule pluginModule = createInstance(moduleClass.get());

                    registerModule(pluginModule);
                }catch (Exception ex){
                    SuperiorSkyblockPlugin.log("Couldn't register module " + moduleName + ": ");
                    new HandlerLoadException(ex, "Couldn't register module " + moduleName + ".", HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
                }
            }
        }
    }

    private PluginModule createInstance(Class<?> clazz) throws Exception{
        Preconditions.checkArgument(PluginModule.class.isAssignableFrom(clazz), "Class " + clazz + " is not a PluginModule.");

        for(Constructor<?> constructor : clazz.getConstructors()){
            if(constructor.getParameterCount() == 0) {
                if(!constructor.isAccessible())
                    constructor.setAccessible(true);

                return (PluginModule) constructor.newInstance();
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private static class ModuleData {

        private final Listener[] listeners;
        private final SuperiorCommand[] commands;
        private final SuperiorCommand[] adminCommands;

        private ModuleData(Listener[] listeners, SuperiorCommand[] commands, SuperiorCommand[] adminCommands){
            this.listeners = listeners;
            this.commands = commands;
            this.adminCommands = adminCommands;
        }

    }

}
