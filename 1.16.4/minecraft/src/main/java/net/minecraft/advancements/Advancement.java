package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
   private final Advancement parent;
   private final DisplayInfo display;
   private final AdvancementRewards rewards;
   private final ResourceLocation id;
   private final Map<String, Criterion> criteria;
   private final String[][] requirements;
   private final Set<Advancement> children = Sets.newLinkedHashSet();
   private final ITextComponent displayText;

   public Advancement(ResourceLocation id, @Nullable Advancement parentIn, @Nullable DisplayInfo displayIn, AdvancementRewards rewardsIn, Map<String, Criterion> criteriaIn, String[][] requirementsIn) {
      this.id = id;
      this.display = displayIn;
      this.criteria = ImmutableMap.copyOf(criteriaIn);
      this.parent = parentIn;
      this.rewards = rewardsIn;
      this.requirements = requirementsIn;
      if (parentIn != null) {
         parentIn.addChild(this);
      }

      if (displayIn == null) {
         this.displayText = new StringTextComponent(id.toString());
      } else {
         ITextComponent itextcomponent = displayIn.getTitle();
         TextFormatting textformatting = displayIn.getFrame().getFormat();
         ITextComponent itextcomponent1 = TextComponentUtils.func_240648_a_(itextcomponent.deepCopy(), Style.EMPTY.setFormatting(textformatting)).appendString("\n").append(displayIn.getDescription());
         ITextComponent itextcomponent2 = itextcomponent.deepCopy().modifyStyle((p_211567_1_) -> {
            return p_211567_1_.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
         });
         this.displayText = TextComponentUtils.wrapWithSquareBrackets(itextcomponent2).mergeStyle(textformatting);
      }

   }

   public Advancement.Builder copy() {
      return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
   }

   @Nullable
   public Advancement getParent() {
      return this.parent;
   }

   @Nullable
   public DisplayInfo getDisplay() {
      return this.display;
   }

   public AdvancementRewards getRewards() {
      return this.rewards;
   }

   public String toString() {
      return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
   }

   public Iterable<Advancement> getChildren() {
      return this.children;
   }

   public Map<String, Criterion> getCriteria() {
      return this.criteria;
   }

   @OnlyIn(Dist.CLIENT)
   public int getRequirementCount() {
      return this.requirements.length;
   }

   public void addChild(Advancement advancementIn) {
      this.children.add(advancementIn);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Advancement)) {
         return false;
      } else {
         Advancement advancement = (Advancement)p_equals_1_;
         return this.id.equals(advancement.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public String[][] getRequirements() {
      return this.requirements;
   }

   public ITextComponent getDisplayText() {
      return this.displayText;
   }

   public static class Builder {
      private ResourceLocation parentId;
      private Advancement parent;
      private DisplayInfo display;
      private AdvancementRewards rewards = AdvancementRewards.EMPTY;
      private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
      private String[][] requirements;
      private IRequirementsStrategy requirementsStrategy = IRequirementsStrategy.AND;

      private Builder(@Nullable ResourceLocation parentIdIn, @Nullable DisplayInfo displayIn, AdvancementRewards rewardsIn, Map<String, Criterion> criteriaIn, String[][] requirementsIn) {
         this.parentId = parentIdIn;
         this.display = displayIn;
         this.rewards = rewardsIn;
         this.criteria = criteriaIn;
         this.requirements = requirementsIn;
      }

      private Builder() {
      }

      public static Advancement.Builder builder() {
         return new Advancement.Builder();
      }

      public Advancement.Builder withParent(Advancement parentIn) {
         this.parent = parentIn;
         return this;
      }

      public Advancement.Builder withParentId(ResourceLocation parentIdIn) {
         this.parentId = parentIdIn;
         return this;
      }

      public Advancement.Builder withDisplay(ItemStack stack, ITextComponent title, ITextComponent description, @Nullable ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
         return this.withDisplay(new DisplayInfo(stack, title, description, background, frame, showToast, announceToChat, hidden));
      }

      public Advancement.Builder withDisplay(IItemProvider itemIn, ITextComponent title, ITextComponent description, @Nullable ResourceLocation background, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
         return this.withDisplay(new DisplayInfo(new ItemStack(itemIn.asItem()), title, description, background, frame, showToast, announceToChat, hidden));
      }

      public Advancement.Builder withDisplay(DisplayInfo displayIn) {
         this.display = displayIn;
         return this;
      }

      public Advancement.Builder withRewards(AdvancementRewards.Builder rewardsBuilder) {
         return this.withRewards(rewardsBuilder.build());
      }

      public Advancement.Builder withRewards(AdvancementRewards rewards) {
         this.rewards = rewards;
         return this;
      }

      public Advancement.Builder withCriterion(String key, ICriterionInstance criterionIn) {
         return this.withCriterion(key, new Criterion(criterionIn));
      }

      public Advancement.Builder withCriterion(String key, Criterion criterionIn) {
         if (this.criteria.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate criterion " + key);
         } else {
            this.criteria.put(key, criterionIn);
            return this;
         }
      }

      public Advancement.Builder withRequirementsStrategy(IRequirementsStrategy strategy) {
         this.requirementsStrategy = strategy;
         return this;
      }

      public boolean resolveParent(Function<ResourceLocation, Advancement> lookup) {
         if (this.parentId == null) {
            return true;
         } else {
            if (this.parent == null) {
               this.parent = lookup.apply(this.parentId);
            }

            return this.parent != null;
         }
      }

      public Advancement build(ResourceLocation id) {
         if (!this.resolveParent((p_199750_0_) -> {
            return null;
         })) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }

            return new Advancement(id, this.parent, this.display, this.rewards, this.criteria, this.requirements);
         }
      }

      public Advancement register(Consumer<Advancement> consumer, String id) {
         Advancement advancement = this.build(new ResourceLocation(id));
         consumer.accept(advancement);
         return advancement;
      }

      public JsonObject serialize() {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
         }

         JsonObject jsonobject = new JsonObject();
         if (this.parent != null) {
            jsonobject.addProperty("parent", this.parent.getId().toString());
         } else if (this.parentId != null) {
            jsonobject.addProperty("parent", this.parentId.toString());
         }

         if (this.display != null) {
            jsonobject.add("display", this.display.serialize());
         }

         jsonobject.add("rewards", this.rewards.serialize());
         JsonObject jsonobject1 = new JsonObject();

         for(Entry<String, Criterion> entry : this.criteria.entrySet()) {
            jsonobject1.add(entry.getKey(), entry.getValue().serialize());
         }

         jsonobject.add("criteria", jsonobject1);
         JsonArray jsonarray1 = new JsonArray();

         for(String[] astring : this.requirements) {
            JsonArray jsonarray = new JsonArray();

            for(String s : astring) {
               jsonarray.add(s);
            }

            jsonarray1.add(jsonarray);
         }

         jsonobject.add("requirements", jsonarray1);
         return jsonobject;
      }

      public void writeTo(PacketBuffer buf) {
         if (this.parentId == null) {
            buf.writeBoolean(false);
         } else {
            buf.writeBoolean(true);
            buf.writeResourceLocation(this.parentId);
         }

         if (this.display == null) {
            buf.writeBoolean(false);
         } else {
            buf.writeBoolean(true);
            this.display.write(buf);
         }

         Criterion.serializeToNetwork(this.criteria, buf);
         buf.writeVarInt(this.requirements.length);

         for(String[] astring : this.requirements) {
            buf.writeVarInt(astring.length);

            for(String s : astring) {
               buf.writeString(s);
            }
         }

      }

      public String toString() {
         return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
      }

      public static Advancement.Builder deserialize(JsonObject json, ConditionArrayParser conditionParser) {
         ResourceLocation resourcelocation = json.has("parent") ? new ResourceLocation(JSONUtils.getString(json, "parent")) : null;
         DisplayInfo displayinfo = json.has("display") ? DisplayInfo.deserialize(JSONUtils.getJsonObject(json, "display")) : null;
         AdvancementRewards advancementrewards = json.has("rewards") ? AdvancementRewards.deserializeRewards(JSONUtils.getJsonObject(json, "rewards")) : AdvancementRewards.EMPTY;
         Map<String, Criterion> map = Criterion.deserializeAll(JSONUtils.getJsonObject(json, "criteria"), conditionParser);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonarray = JSONUtils.getJsonArray(json, "requirements", new JsonArray());
            String[][] astring = new String[jsonarray.size()][];

            for(int i = 0; i < jsonarray.size(); ++i) {
               JsonArray jsonarray1 = JSONUtils.getJsonArray(jsonarray.get(i), "requirements[" + i + "]");
               astring[i] = new String[jsonarray1.size()];

               for(int j = 0; j < jsonarray1.size(); ++j) {
                  astring[i][j] = JSONUtils.getString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (astring.length == 0) {
               astring = new String[map.size()][];
               int k = 0;

               for(String s2 : map.keySet()) {
                  astring[k++] = new String[]{s2};
               }
            }

            for(String[] astring1 : astring) {
               if (astring1.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               for(String s : astring1) {
                  if (!map.containsKey(s)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
                  }
               }
            }

            for(String s1 : map.keySet()) {
               boolean flag = false;

               for(String[] astring2 : astring) {
                  if (ArrayUtils.contains(astring2, s1)) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
               }
            }

            return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring);
         }
      }

      public static Advancement.Builder readFrom(PacketBuffer buf) {
         ResourceLocation resourcelocation = buf.readBoolean() ? buf.readResourceLocation() : null;
         DisplayInfo displayinfo = buf.readBoolean() ? DisplayInfo.read(buf) : null;
         Map<String, Criterion> map = Criterion.criteriaFromNetwork(buf);
         String[][] astring = new String[buf.readVarInt()][];

         for(int i = 0; i < astring.length; ++i) {
            astring[i] = new String[buf.readVarInt()];

            for(int j = 0; j < astring[i].length; ++j) {
               astring[i][j] = buf.readString(32767);
            }
         }

         return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring);
      }

      public Map<String, Criterion> getCriteria() {
         return this.criteria;
      }
   }
}
