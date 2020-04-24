package re.domi.uniq.tweakers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import re.domi.uniq.ForgeEventHandler;
import re.domi.uniq.ResourceUnifier;
import re.domi.uniq.UniQ;
import re.domi.uniq.tweaker.IGeneralTweaker;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MinecraftTweaker implements IGeneralTweaker
{
    @Override
    public String getName()
    {
        return "Minecraft";
    }

    @Override
    public String getModId()
    {
        return "";
    }

    @Override
    public void run(ResourceUnifier unifier) throws Exception
    {
        this.processCraftingRecipes(unifier);
        this.processFurnaceRecipes(unifier);
        this.processChestGen(unifier);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler(unifier));
    }

    private void processCraftingRecipes(ResourceUnifier unifier)
    {
        List<IRecipe> recipes = (List<IRecipe>) CraftingManager.getInstance().getRecipeList();

        UniQ.instance.recipeProcessor.transform(unifier, recipes);
    }

    private void processFurnaceRecipes(ResourceUnifier unifier)
    {
        HashMap<ItemStack, ItemStack> recipes = (HashMap<ItemStack, ItemStack>) FurnaceRecipes.smelting().getSmeltingList();

        for (Map.Entry<ItemStack, ItemStack> entry : recipes.entrySet())
        {
            entry.setValue(unifier.getPreferredStack(entry.getValue()));
        }
    }

    private void processChestGen(ResourceUnifier unifier) throws Exception
    {
        Field fChestInfo = ChestGenHooks.class.getDeclaredField("chestInfo");
        Field fContents = ChestGenHooks.class.getDeclaredField("contents");

        fChestInfo.setAccessible(true);
        fContents.setAccessible(true);

        HashMap<String, ChestGenHooks> chestInfo = (HashMap<String, ChestGenHooks>) fChestInfo.get(null);

        for (ChestGenHooks chestGenHooks : chestInfo.values())
        {
            List<WeightedRandomChestContent> contents = (List<WeightedRandomChestContent>) fContents.get(chestGenHooks);

            for (WeightedRandomChestContent content : contents)
            {
                content.theItemId = unifier.getPreferredStack(content.theItemId);
            }
        }
    }
}
