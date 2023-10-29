package one.oth3r.directionhud;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.text.Text;
import one.oth3r.directionhud.common.Assets;
import one.oth3r.directionhud.common.Destination;
import one.oth3r.directionhud.common.HUD;
import one.oth3r.directionhud.common.files.config;
import one.oth3r.directionhud.common.utils.CUtl;
import one.oth3r.directionhud.utils.CTxT;
import one.oth3r.directionhud.utils.Utl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModMenu implements ModMenuApi {
    private static CTxT lang(String key, Object... args) {
        return CUtl.lang("config."+key, args);
    }
    private static ArrayList<Color> toColorList(List<String> list) {
        ArrayList<Color> colors = new ArrayList<>();
        for (String s:list) colors.add(Color.decode(s));
        return colors;
    }
    private static ArrayList<String> toStringList(List<Color> list) {
        ArrayList<String> strings = new ArrayList<>();
        for (Color c:list) strings.add(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
        return strings;
    }
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> YetAnotherConfigLib.createBuilder().save(config::save)
                .title(Text.of("DirectionHUD"))
                .category(ConfigCategory.createBuilder()
                        .name(lang("main").b())
                        .tooltip(lang("main.info").b())
                        .option(Option.<Integer>createBuilder()
                                .name(lang("max.xz").b())
                                .description(OptionDescription.of(lang("max.info",lang("max.xz.info").color(CUtl.s())).b()))
                                .binding(config.defaults.MAXxz, () -> config.MAXxz, n -> config.MAXxz = n)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(lang("max.y").b())
                                .description(OptionDescription.of(lang("max.info",lang("max.y.info").color(CUtl.s())).b()))
                                .binding(config.defaults.MAXy, () -> config.MAXy, n -> config.MAXy = n)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(lang("online_mode").b())
                                .description(OptionDescription.of(lang("online_mode.info").b()))
                                .binding(config.defaults.online, () -> config.online, n -> config.online = n)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("dimensions").b())
                                .description(OptionDescription.of(lang("dimensions.info").append("\n")
                                        .append(lang("dimensions.info_2").color('c')).append("\n\n")
                                        .append(lang("dimensions.info_3",lang("dimensions.info_3.1").color('a'),
                                                lang("dimensions.info_3.2").color('b'),lang("dimensions.info_3.3").color('9'))).append("\n\n")
                                        .append(lang("dimensions.info_4").color('a')).append("\n")
                                        .append(lang("dimensions.info_5").color('b')).append("\n")
                                        .append(lang("dimensions.info_6").color('9')).b()))
                                .binding(config.defaults.dimensions, () -> config.dimensions, n -> config.dimensions = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("dimension_ratios").b())
                                .description(OptionDescription.of(lang("dimension_ratios.info").append("\n\n")
                                        .append(lang("dimension_ratios.info_2",lang("dimension_ratios.info_2.1").color('a'),
                                                lang("dimension_ratios.info_2.2").color('b'))).b()))
                                .binding(config.defaults.dimensionRatios, () -> config.dimensionRatios, n -> config.dimensionRatios = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(CUtl.lang("dest").b())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("dest_saving").b())
                                        .description(OptionDescription.of(lang("dest_saving.info").b()))
                                        .binding(config.defaults.DESTSaving, () -> config.DESTSaving, n -> config.DESTSaving = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(lang("dest_max_saved").b())
                                        .description(OptionDescription.of(lang("dest_max_saved.info").b()))
                                        .binding(config.defaults.MAXSaved, () -> config.MAXSaved, n -> config.MAXSaved = n)
                                        .controller(IntegerFieldControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("social").b())
                                        .description(OptionDescription.of(lang("social.info").append("\n").append(lang("social.info_2").color('7')).b()))
                                        .binding(config.defaults.social, () -> config.social, n -> config.social = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("death_saving").b())
                                        .description(OptionDescription.of(lang("death_saving.info").b()))
                                        .binding(config.defaults.deathsaving, () -> config.deathsaving, n -> config.deathsaving = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(CUtl.lang("hud").b())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud_editing").b())
                                        .description(OptionDescription.of(lang("hud_editing.info").b()))
                                        .binding(config.defaults.HUDEditing, () -> config.HUDEditing, n -> config.HUDEditing = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(lang("hud_refresh").b())
                                        .description(OptionDescription.of(lang("hud_refresh.info").b()))
                                        .binding(config.defaults.HUDRefresh, () -> config.HUDRefresh, n -> config.HUDRefresh = n)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).step(1).range(1,20))
                                        .build())
                                .build())
                        .group(ListOption.<Color>createBuilder()
                                .name(lang("color_presets").b())
                                .binding(toColorList(config.defaults.colorPresets), () -> toColorList(config.colorPresets), n -> config.colorPresets = toStringList(n))
                                .controller(ColorControllerBuilder::create)
                                .initial(Color.WHITE)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(lang("hud").b())
                        .tooltip(lang("hud.info").append("\n").append(lang("defaults.info").color('c')).b())
                        .option(Option.<Boolean>createBuilder()
                                .name(lang("hud.enabled").b())
                                .description(OptionDescription.of(lang("hud.enabled.info").b()))
                                .binding(config.hud.defaults.Enabled, () -> config.hud.Enabled, n -> config.hud.Enabled = n)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("hud.order").b())
                                .description(OptionDescription.of(lang("hud.order.info").b()))
                                .binding(HUD.Module.toStringList(config.hud.defaults.Order), () -> HUD.Module.toStringList(config.hud.Order), n -> config.hud.Order = HUD.Module.toModuleList(new ArrayList<>(n)))
                                .controller(StringControllerBuilder::create)
                                // CHANGE THIS WHEN MORE MODULES ARE OUT
                                .maximumNumberOfEntries(7)
                                .initial("")
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("hud.module").b())
                                .description(OptionDescription.of(lang("hud.module.info").b()))
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.coordinates").b())
                                        .binding(config.hud.defaults.Coordinates, () -> config.hud.Coordinates, n -> config.hud.Coordinates = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.destination").b())
                                        .binding(config.hud.defaults.Destination, () -> config.hud.Destination, n -> config.hud.Destination = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.distance").b())
                                        .binding(config.hud.defaults.Distance, () -> config.hud.Distance, n -> config.hud.Distance = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.tracking").b())
                                        .binding(config.hud.defaults.Tracking, () -> config.hud.Tracking, n -> config.hud.Tracking = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.direction").b())
                                        .binding(config.hud.defaults.Direction, () -> config.hud.Direction, n -> config.hud.Direction = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.time").b())
                                        .binding(config.hud.defaults.Time, () -> config.hud.Time, n -> config.hud.Time = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("hud.module.weather").b())
                                        .binding(config.hud.defaults.Weather, () -> config.hud.Weather, n -> config.hud.Weather = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("settings").b())
                                .description(OptionDescription.of(lang("hud.settings.info").b()))
                                .option(Option.<HUD.Setting.DisplayType>createBuilder()
                                        .name(lang("hud.settings.type").b())
                                        .binding(HUD.Setting.DisplayType.get(config.hud.defaults.DisplayType), () -> HUD.Setting.DisplayType.get(config.hud.DisplayType), n -> config.hud.DisplayType = n.toString())
                                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(HUD.Setting.DisplayType.class)
                                                .formatValue(v -> CUtl.lang("hud.settings.type."+v.name().toLowerCase()).b()))
                                        .build())
                                .option(Option.<HUD.Setting.BarColor>createBuilder()
                                        .name(lang("hud.settings.bossbar.color").b())
                                        .binding(HUD.Setting.BarColor.get(config.hud.defaults.BarColor), () -> HUD.Setting.BarColor.get(config.hud.BarColor), n -> config.hud.BarColor = n.toString())
                                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(HUD.Setting.BarColor.class)
                                                .formatValue(v -> CUtl.lang("hud.settings.bossbar.color."+v.name().toLowerCase()).color(Assets.barColor(v)).b()))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.settings.bossbar.distance").b())
                                        .binding(config.hud.defaults.BarShowDistance, () -> config.hud.BarShowDistance, n -> config.hud.BarShowDistance = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(lang("hud.settings.bossbar.distance_max").b())
                                        .description(OptionDescription.of(lang("hud.settings.bossbar.distance_max.info").b()))
                                        .binding(config.hud.defaults.ShowDistanceMAX, () -> config.hud.ShowDistanceMAX, n -> config.hud.ShowDistanceMAX = n)
                                        .controller(IntegerFieldControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.settings.module.time_24hr").b())
                                        .binding(config.hud.defaults.Time24HR, () -> config.hud.Time24HR, n -> config.hud.Time24HR = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .option(Option.<HUD.Setting.HUDTrackingTarget>createBuilder()
                                        .name(lang("hud.settings.module.tracking_target").b())
                                        .binding(HUD.Setting.HUDTrackingTarget.get(config.hud.defaults.TrackingTarget), () -> HUD.Setting.HUDTrackingTarget.get(config.hud.TrackingTarget), n -> config.hud.TrackingTarget = n.toString())
                                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(HUD.Setting.HUDTrackingTarget.class)
                                                .formatValue(v -> CUtl.lang("hud.settings.module.tracking_target."+v.name().toLowerCase()).b()))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("hud.color.category",Utl.capitalizeFirst(CUtl.lang("hud.color.primary").toString())).b())
                                .description(OptionDescription.of(lang("hud.color.category.info",CUtl.lang("hud.color.primary")).b()))
                                .option(Option.<Color>createBuilder()
                                        .name(lang("hud.color").b())
                                        .binding(Color.decode(CUtl.color.format(config.hud.defaults.primary.Color)),() -> Color.decode(CUtl.color.format(config.hud.primary.Color)),
                                                n -> config.hud.primary.Color = String.format("#%02x%02x%02x", n.getRed(), n.getGreen(), n.getBlue()))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.color.bold").b())
                                        .binding(config.hud.defaults.primary.Bold, () -> config.hud.primary.Bold, n -> config.hud.primary.Bold = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.color.italics").b())
                                        .binding(config.hud.defaults.primary.Italics, () -> config.hud.primary.Italics, n -> config.hud.primary.Italics = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.color.rainbow").b())
                                        .binding(config.hud.defaults.primary.Rainbow, () -> config.hud.primary.Rainbow, n -> config.hud.primary.Rainbow = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("hud.color.category",Utl.capitalizeFirst(CUtl.lang("hud.color.secondary").toString())).b())
                                .description(OptionDescription.of(lang("hud.color.category.info",CUtl.lang("hud.color.secondary")).b()))
                                .option(Option.<Color>createBuilder()
                                        .name(lang("hud.color").b())
                                        .binding(Color.decode(CUtl.color.format(config.hud.defaults.secondary.Color)),() -> Color.decode(CUtl.color.format(config.hud.secondary.Color)),
                                                n -> config.hud.secondary.Color = String.format("#%02x%02x%02x", n.getRed(), n.getGreen(), n.getBlue()))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.color.bold").b())
                                        .binding(config.hud.defaults.secondary.Bold, () -> config.hud.secondary.Bold, n -> config.hud.secondary.Bold = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.color.italics").b())
                                        .binding(config.hud.defaults.secondary.Italics, () -> config.hud.secondary.Italics, n -> config.hud.secondary.Italics = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hud.color.rainbow").b())
                                        .binding(config.hud.defaults.secondary.Italics, () -> config.hud.secondary.Rainbow, n -> config.hud.secondary.Rainbow = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(lang("dest").b())
                        .tooltip(lang("dest.info").append("\n").append(lang("defaults.info").color('c')).b())
                        .group(OptionGroup.createBuilder()
                                .name(lang("settings").b())
                                .description(OptionDescription.of(lang("dest.settings.info").b()))
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.autoclear").b())
                                        .binding(config.dest.defaults.AutoClear, () -> config.dest.AutoClear, n -> config.dest.AutoClear = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(lang("dest.settings.autoclear_rad").b())
                                        .binding(config.dest.defaults.AutoClearRad, () -> config.dest.AutoClearRad, n -> config.dest.AutoClearRad = n)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).step(1).range(1, 15))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.autoconvert").b())
                                        .binding(config.dest.defaults.AutoConvert, () -> config.dest.AutoConvert, n -> config.dest.AutoConvert = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.ylevel").b())
                                        .binding(config.dest.defaults.YLevel, () -> config.dest.YLevel, n -> config.dest.YLevel = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("dest.settings.particles").b())
                                .description(OptionDescription.of(lang("dest.settings.particles.info").b()))
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.particles.dest").b())
                                        .binding(config.dest.defaults.particles.Dest, () -> config.dest.particles.Dest, n -> config.dest.particles.Dest = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(lang("dest.settings.particles.color",CUtl.lang("dest.settings.particles.dest")).b())
                                        .binding(Color.decode(CUtl.color.format(config.dest.defaults.particles.DestColor)),() -> Color.decode(CUtl.color.format(config.dest.particles.DestColor)),
                                                n -> config.dest.particles.DestColor = String.format("#%02x%02x%02x", n.getRed(), n.getGreen(), n.getBlue()))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.particles.line").b())
                                        .binding(config.dest.defaults.particles.Line, () -> config.dest.particles.Line, n -> config.dest.particles.Line = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(lang("dest.settings.particles.color",CUtl.lang("dest.settings.particles.line")).b())
                                        .binding(Color.decode(CUtl.color.format(config.dest.defaults.particles.LineColor)),() -> Color.decode(CUtl.color.format(config.dest.particles.LineColor)),
                                                n -> config.dest.particles.LineColor = String.format("#%02x%02x%02x", n.getRed(), n.getGreen(), n.getBlue()))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.particles.tracking").b())
                                        .binding(config.dest.defaults.particles.Tracking, () -> config.dest.particles.Tracking, n -> config.dest.particles.Tracking = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(lang("dest.settings.particles.color",CUtl.lang("dest.settings.particles.tracking")).b())
                                        .binding(Color.decode(CUtl.color.format(config.dest.defaults.particles.TrackingColor)),() -> Color.decode(CUtl.color.format(config.dest.particles.TrackingColor)),
                                                n -> config.dest.particles.TrackingColor = String.format("#%02x%02x%02x", n.getRed(), n.getGreen(), n.getBlue()))
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("dest.settings.features").b())
                                .description(OptionDescription.of(lang("dest.settings.features.info").b()))
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.features.send").b())
                                        .binding(config.dest.defaults.Send, () -> config.dest.Send, n -> config.dest.Send = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.features.track").b())
                                        .binding(config.dest.defaults.Track, () -> config.dest.Track, n -> config.dest.Track = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Destination.Setting.TrackingRequestMode>createBuilder()
                                        .name(lang("dest.settings.features.track_request_mode").b())
                                        .description(OptionDescription.of(lang("dest.settings.features.track_request_mode.info",
                                                CUtl.lang("dest.settings.features.track_request_mode.instant").color(CUtl.s()),CUtl.lang("dest.settings.features.track_request_mode.instant.info")).append("\n")
                                                .append(lang("dest.settings.features.track_request_mode.info",
                                                        CUtl.lang("dest.settings.features.track_request_mode.request").color(CUtl.s()),CUtl.lang("dest.settings.features.track_request_mode.request.info"))).b()))
                                        .binding(Destination.Setting.TrackingRequestMode.get(config.dest.defaults.TrackingRequestMode), () -> Destination.Setting.TrackingRequestMode.get(config.dest.TrackingRequestMode), n -> config.dest.TrackingRequestMode = n.toString())
                                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Destination.Setting.TrackingRequestMode.class)
                                                .formatValue(v -> CUtl.lang("dest.settings.features.track_request_mode."+v.name().toLowerCase()).b()))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(CUtl.lang("dest.settings.features.lastdeath").b())
                                        .binding(config.dest.defaults.Lastdeath, () -> config.dest.Lastdeath, n -> config.dest.Lastdeath = n)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .build().generateScreen(parent);
    }
}
