package com.qsage.client.gui.screen;

import com.qsage.SmartEconomy;
import com.qsage.client.economy.ClientEconomy;
import com.qsage.client.economy.MoneyFormatter;
import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.PlayerHeadRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;


public final class BankPanel {
    private static final int LEFT_X = 18;
    private static final int RIGHT_X = 161;
    private static final int FIRST_Y = 53;
    private static final int ROW_WIDTH = 108;
    private static final int ROW_TEXTURE_HEIGHT = 22;
    private static final int ROW_SPACING = 7;
    private static final int ROW_HEIGHT = ROW_TEXTURE_HEIGHT + ROW_SPACING;
    private static final int ROWS_PER_COLUMN = 4;
    private static final int PAGE_SIZE = ROWS_PER_COLUMN * 2;


    private final EconomyScreen screen;
    private final List<BankPlayer> players = new ArrayList<>();
    private Consumer<BankPlayer> onRecipientSelected = player -> {};
    private Consumer<UUID> onFavoriteToggle = uuid -> {};
    private boolean visible;
    private String searchText = "";
    private int scrollOffset;

    public BankPanel(EconomyScreen screen) {
        this.screen = screen;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPlayers(List<BankPlayer> updatedPlayers) {
        players.clear();
        players.addAll(updatedPlayers);
        scrollOffset = Math.min(scrollOffset, maxScroll());
    }

    public void setSearchText(String text) {
        searchText = text == null ? "" : text;
        scrollOffset = 0;
    }

    public void setOnRecipientSelected(Consumer<BankPlayer> callback) {
        onRecipientSelected = callback;
    }

    public void setOnFavoriteToggle(Consumer<UUID> callback) {
        onFavoriteToggle = callback;
    }

    private void renderPlayerName(
            GuiGraphicsExtractor graphics,
            BankPlayer player,
            int x,
            int y
    ) {
        var font = Minecraft.getInstance().font;


        String name = font.plainSubstrByWidth(
                player.name(),
                67
        );

        graphics.text(
                font,
                name,
                x + 23,
                y + 3,
                0xFFFFFFFF,
                false
        );
    }


    private List<BankPlayer> filtered() {
        String query = searchText.trim()
                .toLowerCase(Locale.ROOT);

        return players.stream()
                .filter(player ->
                        player.name()
                                .toLowerCase(Locale.ROOT)
                                .contains(query)
                )
                .sorted(
                        Comparator
                                .comparing(BankPlayer::favorite)
                                .reversed()
                                .thenComparing(
                                        Comparator.comparing(
                                                BankPlayer::online
                                        ).reversed()
                                )
                                .thenComparing(
                                        BankPlayer::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }


    private int maxScroll() {
        return Math.max(0, filtered().size() - PAGE_SIZE);
    }



    private void renderAccount(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        var font = minecraft.font;

        int x = screen.guiLeft() + 2;
        int y = screen.guiTop() + 6;

        // =========================
        // КООРДИНАТЫ
        // =========================

        int headX = x + 19;
        int headY = y + 13;
        int headSize = 24;

        int nameX = headX + headSize + 5;
        int bottomY = headY + headSize;

        int valueY = bottomY - font.lineHeight + 2;

        int nameY = valueY;
        int balanceY = valueY;


        int availableRight = x + 126;
        int lockedRight = x + 269;

        int labelY = y + 12;


        // =========================
        // ПРОФИЛЬ ИГРОКА
        // =========================

        PlayerHeadRenderer.render(
                graphics,
                minecraft.player.getUUID(),
                headX,
                headY,
                headSize
        );

        String playerName = minecraft.player
                .getGameProfile()
                .name();


        int nameRight = x + 91;
        int maxNameWidth = Math.max(0, nameRight - nameX);

        String displayName = playerName;

        if (font.width(playerName) > maxNameWidth) {
            String ellipsis = "…";

            displayName = font.plainSubstrByWidth(
                    playerName,
                    Math.max(0, maxNameWidth - font.width(ellipsis))
            ) + ellipsis;
        }


        graphics.text(
                font,
                displayName,
                nameX,
                nameY,
                0xFFFFFFFF,
                false
        );


        if (!displayName.equals(playerName)
                && inside(
                mouseX,
                mouseY,
                nameX,
                nameY,
                font.width(displayName),
                font.lineHeight
        )) {

            graphics.setTooltipForNextFrame(
                    font,
                    Component.literal(playerName),
                    mouseX,
                    mouseY
            );
        }


        // =========================
        // БАЛАНС
        // =========================


        long available = ClientEconomy.getAvailable();
        long locked = ClientEconomy.getLocked();

        Component availableLabel = Component.translatable(
                "gui.smart_economy.bank.available"
        );

        Component lockedLabel = Component.translatable(
                "gui.smart_economy.bank.locked"
        );

        String availableText = MoneyFormatter.format(available);
        String lockedText = MoneyFormatter.format(locked);

        // Вычисляем позиции с учётом ширины текста
        int availableLabelX = availableRight - font.width(availableLabel);
        int availableValueX = availableRight - font.width(availableText);

        int lockedLabelX = lockedRight - font.width(lockedLabel) - 6;
        int lockedValueX = lockedRight - font.width(lockedText) - 6;

        // =========================
        // ДОСТУПНЫЙ БАЛАНС
        // =========================

        graphics.text(
                font,
                availableLabel,
                availableLabelX,
                labelY,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                availableText,
                availableValueX,
                balanceY,
                0xffffd700,
                false
        );

        // =========================
        // ЗАБЛОКИРОВАННЫЕ СРЕДСТВА
        // =========================

        graphics.text(
                font,
                lockedLabel,
                lockedLabelX,
                labelY,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                lockedText,
                lockedValueX,
                balanceY,
                0xFFbe0119,
                false
        );

        // =========================
        // TOOLTIPS
        // =========================

        if (inside(
                mouseX,
                mouseY,
                availableValueX,
                balanceY,
                font.width(availableText),
                font.lineHeight
        )) {
            graphics.setTooltipForNextFrame(
                    font,
                    Component.literal(
                            MoneyFormatter.formatFull(available)
                    ),
                    mouseX,
                    mouseY
            );
        }

        if (inside(
                mouseX,
                mouseY,
                lockedValueX,
                balanceY,
                font.width(lockedText),
                font.lineHeight
        )) {
            graphics.setTooltipForNextFrame(
                    font,
                    Component.literal(
                            MoneyFormatter.formatFull(locked)
                    ),
                    mouseX,
                    mouseY
            );
        }
    }




    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        if (!visible) return;

        int x = screen.guiLeft();
        int y = screen.guiTop();

        var font = Minecraft.getInstance().font;

        GuiAtlas.draw(
                graphics,
                GuiStyle.BANK_ACCOUNT_BACKGROUND,
                x + 18,
                y + 13
        );

        renderAccount(graphics, mouseX, mouseY);

        List<BankPlayer> filtered = filtered();

        int end = Math.min(
                filtered.size(),
                scrollOffset + PAGE_SIZE
        );

        for (int index = scrollOffset; index < end; index++) {

            int visibleIndex = index - scrollOffset;

            int column = visibleIndex % 2;
            int row = visibleIndex / 2;

            int rx = x + (
                    column == 0 ? LEFT_X : RIGHT_X
            );

            // 22 px текстура + 7 px отступ
            int ry = y + FIRST_Y + row * ROW_HEIGHT;

            BankPlayer player = filtered.get(index);

            // Фон строки из атласа
            GuiAtlas.draw(
                    graphics,
                    GuiStyle.BANK_PLAYER_ROW,
                    rx,
                    ry
            );

            renderPlayerName(graphics, player, rx, ry);

            PlayerHeadRenderer.render(
                    graphics,
                    player.uuid(),
                    rx + 3,
                    ry + 3,
                    16
            );

            graphics.text(
                    font,
                    Component.translatable(
                            player.online()
                                    ? "gui.smart_economy.bank.online"
                                    : "gui.smart_economy.bank.offline"
                    ),
                    rx + 23,
                    ry + 12,
                    player.online() ? 0xFF35E06B : 0xFFD5D5D5,
                    false
            );


            String star = player.favorite() ? "★" : "☆";

            int starX = rx + 96;
            int starY = ry + (ROW_TEXTURE_HEIGHT - font.lineHeight) / 2;

            graphics.text(
                    font,
                    star,
                    starX,
                    starY,
                    0xFFFFD34D,
                    false
            );
        }



        if (filtered.isEmpty()) {
            graphics.text(
                    font,
                    Component.translatable(
                            "gui.smart_economy.bank.no_players"
                    ),
                    x + LEFT_X,
                    y + FIRST_Y + 5,
                    0x777777,
                    false
            );
        }


    }


    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || scrollY == 0 || !insideList(mouseX, mouseY)) return false;

        scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset - (int) Math.signum(scrollY)));

        return true;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || button != 0) {
            return false;
        }

        int x = screen.guiLeft();
        int y = screen.guiTop();

        List<BankPlayer> filtered = filtered();

        int end = Math.min(
                filtered.size(),
                scrollOffset + PAGE_SIZE
        );

        for (int index = scrollOffset; index < end; index++) {

            int visibleIndex = index - scrollOffset;

            // Заполнение слева направо, затем следующая строка
            int column = visibleIndex % 2;
            int row = visibleIndex / 2;

            int rx = x + (column == 0 ? LEFT_X : RIGHT_X);
            int ry = y + FIRST_Y + row * ROW_HEIGHT;

            if (!inside(
                    mouseX,
                    mouseY,
                    rx,
                    ry,
                    ROW_WIDTH,
                    ROW_TEXTURE_HEIGHT
            )) {
                continue;
            }

            BankPlayer player = filtered.get(index);

            // Нажатие на звёздочку
            if (mouseX >= rx + 94) {
                onFavoriteToggle.accept(player.uuid());
            } else {
                // Нажатие на игрока — открытие перевода
                onRecipientSelected.accept(player);
            }

            return true;
        }

        return false;
    }


    private boolean insideList(double mx, double my) {
        return inside(mx, my, screen.guiLeft() + LEFT_X, screen.guiTop() + FIRST_Y, 269, ROWS_PER_COLUMN * ROW_HEIGHT);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
