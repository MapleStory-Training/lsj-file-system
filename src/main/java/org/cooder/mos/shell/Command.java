package org.cooder.mos.shell;

import org.cooder.mos.api.IFile;
import org.cooder.mos.api.Impl.IFileImpl;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.List;

/**
 * @description:
 * @author: lishujiang
 * @date: 2021/05/23 11:23
 **/
public class Command {

    static IFile iFile = new IFileImpl();

    // 将所有子命令装载在一起
    @CommandLine.Command(
            name = "mos-nil",
            version = "mos-nil 1.0",
            subcommands = {
                    Command.touchCommand.class,
                    Command.mkdirCommand.class,
                    Command.lsCommand.class,
                    Command.cdCommand.class,
                    Command.echoCommand.class,
                    Command.catCommand.class,
                    Command.formatCommand.class,
                    Command.HelpCommand.class
            },
            mixinStandardHelpOptions = true
    )
    public static class RootCommand {
    }


    @CommandLine.Command(
            subcommands = {},
            name = "touch",
            description = "创建文件",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class touchCommand implements Runnable {
        @CommandLine.Parameters(
                description = "输入文件名称",
                index = "0"
        )
        private String fileName;

        @Override
        public void run() {
            IFile iFile = new IFileImpl();
            iFile.createFile(fileName);
        }
    }

    @CommandLine.Command(
            name = "mkdir",
            description = "创建文件夹",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class mkdirCommand implements Runnable {
        @CommandLine.Parameters(
                description = "输入文件夹名称"
        )
        private String folderName;

        @Override
        public void run() {
            iFile.mkdir(folderName);
        }
    }

    @CommandLine.Command(
            name = "ls",
            description = "查看文件内容",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class lsCommand implements Runnable {

        @Override
        public void run() {
            iFile.ls();
        }
    }

    @CommandLine.Command(
            name = "cd",
            description = "切换目录",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class cdCommand implements Runnable {
        @CommandLine.Parameters(
                description = "输入文件夹名称"
        )
        private String folderName;

        @Override
        public void run() {
            iFile.cd(folderName);
        }
    }

    @CommandLine.Command(
            name = "echo",
            description = "输出",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class echoCommand implements Runnable {
        @CommandLine.Parameters(
                description = "文件内容"
        )
        private String content;

        @CommandLine.Parameters(
                description = "符号"
        )
        private String symbol;

        @CommandLine.Parameters(
                description = "文件名称"
        )
        private String filename;

        @Override
        public void run() {
            iFile.echo(content, filename);
        }
    }

    @CommandLine.Command(
            name = "cat",
            description = "cat命令",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class catCommand implements Runnable {

        @CommandLine.Parameters
        List<String> positional;

        @Override
        public void run() {
            if (positional.size() == 3){
                iFile.cat(positional.get(0), positional.get(2));
            }else {
                iFile.cat(positional.get(0), null);
            }
        }
    }

    @CommandLine.Command(
            name = "format",
            description = "格式化",
            sortOptions = false,
            mixinStandardHelpOptions = true
    )
    public static class formatCommand implements Runnable {

        @Override
        public void run() {
            iFile.format();
        }
    }

    @CommandLine.Command(name = "help",
            helpCommand = true, description = "帮助")
    public static class HelpCommand implements CommandLine.IHelpCommandInitializable2, Runnable {

        private CommandLine self;
        private PrintWriter out;

        @Override
        public void run() {
            CommandLine parent = self == null ? null : self.getParent();
            if (parent == null) {
                return;
            }
            parent.usage(out);
        }

        @Override
        public void init(CommandLine helpCommandLine, CommandLine.Help.ColorScheme colorScheme, PrintWriter outWriter, PrintWriter errWriter) {
            this.self = helpCommandLine;
            this.out = outWriter;
        }
    }
}
