package Tenzinn.Commands.Roles;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RoleCommands extends AbstractCommandCollection {

    public RoleCommands(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);

        addSubCommand(new SetRoleCommand("set", "Assign --role of a specific player"));
        addSubCommand(new GetRoleCommand("get", "View list of roles"));
    }
}