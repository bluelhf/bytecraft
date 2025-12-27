package blue.lhf.bytecraft.library.plugin_hook;

import blue.lhf.bytecraft.library.plugin_hook.description.MemberPlugin;
import blue.lhf.bytecraft.runtime.BukkitHook;
import com.fasterxml.jackson.annotation.*;
import org.byteskript.skript.api.SyntaxElement;
import org.byteskript.skript.compiler.structure.BasicTree;
import org.byteskript.skript.compiler.structure.SectionMeta;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * A ByteSkript tree structure that holds plugin description information when in a plugin description section ({@link MemberPlugin}).
 *
 * @see <a href="https://docs.papermc.io/paper/dev/plugin-yml/">PaperMC <code>plugin.yml</code> Reference</a>
 * */
@JsonAutoDetect(
        getterVisibility = NONE, fieldVisibility = NONE,
        setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescriptionTree extends BasicTree {
    @JsonProperty("main")
    private final String main = BukkitHook.COMPILED_HOOK_TYPE.dotPath();

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("version")
    private String version = null;

    @JsonProperty("api-version")
    private String apiVersion = null;

    @JsonProperty("description")
    private String description = null;

    public DescriptionTree(final SectionMeta owner) {
        super(owner);
    }

    /**
     * @return The 'name' property of the plugin description.
     * */
    public String getName() {
        return name;
    }

    /**
     * Sets the 'name' property of the plugin description to the given value.
     * @param name The new name.
     * */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return The 'version' property of the plugin description.
     * */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the 'version' property of the plugin description to the given value.
     * @param version The new version.
     * */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return The 'api-version' property of the plugin description.
     * */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets the 'api-version' property of the plugin description to the given value.
     * @param apiVersion The new API version.
     * */
    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return The 'description' property of the plugin description.
     * */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the 'description' property of the plugin description to the given value.
     * @param description The new description.
     * */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return A list of field names, as defined in a PaperMC <code>plugin.yml</code>, that are mandatory but have not been declared.
     * */
    public List<String> getMissingMandatoryFields() {
        final List<String> fields = new ArrayList<>();
        if (name == null) fields.add("name");
        if (version == null) fields.add("version");
        return fields;
    }

    @Override
    public boolean permit(final SyntaxElement element) {
        return element instanceof PluginDescriptionEntry;
    }
}
