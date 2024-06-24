package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidTask extends Task {
	public static final ResourceLocation TANK_TEXTURE = FTBQuestsAPI.rl("textures/tasks/tank.png");
	private static final FluidStack WATER = FluidStack.create(Fluids.WATER, FluidStack.bucketAmount());

	private FluidStack fluidStack = FluidStack.create(Fluids.WATER, FluidStack.bucketAmount());

	public FluidTask(long id, Quest quest) {
		super(id, quest);
	}

	public Fluid getFluid() {
		return fluidStack.getFluid();
	}

	public FluidTask setFluid(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
		return this;
	}

	public DataComponentMap getFluidDataComponents() {
		return fluidStack.getComponents();
	}

	public DataComponentPatch getFluidDataComponentPatch() {
		return fluidStack.getComponents().asPatch();
	}

	@Override
	public TaskType getType() {
		return TaskTypes.FLUID;
	}

	@Override
	public long getMaxProgress() {
		return fluidStack.getAmount();
	}

	@Override
	public String formatMaxProgress() {
		return getVolumeString(fluidStack.getAmount());
	}

	@Override
	public String formatProgress(TeamData teamData, long progress) {
		return getVolumeString((int) Math.min(Integer.MAX_VALUE, progress));
	}

	@Override
	public boolean consumesResources() {
		return true;
	}

	@Override
	public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.writeData(nbt, provider);

		nbt.put("fluid", fluidStack.write(provider, new CompoundTag()));
	}

	@Override
	public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
		super.readData(nbt, provider);

		if (nbt.contains("fluid", Tag.TAG_STRING)) {
			// legacy - fluid stored as string ID
			ResourceLocation id = ResourceLocation.tryParse(nbt.getString("fluid"));
			if (id == null) {
				fluidStack = FluidStack.create(Fluids.WATER, 1000L);
			} else {
				fluidStack = FluidStack.create(BuiltInRegistries.FLUID.get(id), nbt.getLong("amount"));
			}
		} else {
			fluidStack = FluidStack.read(provider, nbt.getCompound("fluid")).orElse(FluidStack.empty());
		}
	}

	@Override
	public void writeNetData(RegistryFriendlyByteBuf buffer) {
		super.writeNetData(buffer);

		fluidStack.write(buffer);
	}

	@Override
	public void readNetData(RegistryFriendlyByteBuf buffer) {
		super.readNetData(buffer);

		fluidStack = FluidStack.read(buffer);
	}

	public static String getVolumeString(long a) {
		StringBuilder builder = new StringBuilder();

		if (a >= FluidStack.bucketAmount()) {
			if (a % FluidStack.bucketAmount() != 0L) {
				builder.append(StringUtils.formatDouble(a / (double) FluidStack.bucketAmount()));
			} else {
				builder.append(a / FluidStack.bucketAmount());
			}
			builder.append(" B");
		} else {
			builder.append(a).append(" mB");
		}

		return builder.toString();
	}

	@Override
	public MutableComponent getAltTitle() {
		return Component.literal(getVolumeString(fluidStack.getAmount()) + " of ").append(fluidStack.getName());
	}

	@Override
	public Icon getAltIcon() {
		return Icon.getIcon(ClientUtils.getStillTexture(fluidStack)).withTint(Color4I.rgb(ClientUtils.getFluidColor(fluidStack)));
	}

	@Override
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);

		config.addFluidStack("fluid", fluidStack, v -> fluidStack = v, WATER, false);
	}

	@Override
	public boolean canInsertItem() {
		return true;
	}

	@Override
	@Nullable
	public Optional<PositionedIngredient> getIngredient(Widget widget) {
		return PositionedIngredient.of(fluidStack, widget);
	}
}
