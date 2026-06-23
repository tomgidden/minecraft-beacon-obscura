package cx.gid.minecraft.beaconobscura.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

    // inside BeaconBlockEntity.tick(), intercept calls to `BlockState.getLightDampening()`
    // on the server side, and redirect them to the alternative method 
    // below that that returns 0 if the block is tinted glass, rather 
    // than the vanilla behavior of tinted glass's getLightDampening()
    // that returns 15 (opaque).
    //
    // We still want tinted glass to _usually_ return 15 so it behaves
    // as expected in most situations, but only act as transparent in
    // the case of a beacon beam collision on the server-side.
    // Otherwise, we'd just patch TintedGlassBlock.getLightDampening()
    // to always return 0, which would fulfill this mod's goal, but
    // would also break the vanilla behavior of tinted glass.
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getLightDampening()I"
        )
    )
    private static int beaconobscura$redirectLightDampening(BlockState state, @Local(argsOnly = true) Level level) {
        if (state.is(Blocks.TINTED_GLASS) && !level.isClientSide()) {
            return 0;
        }
        return state.getLightDampening();
    }
}
