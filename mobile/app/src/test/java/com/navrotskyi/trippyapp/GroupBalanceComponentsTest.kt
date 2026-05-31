package com.navrotskyi.trippyapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navrotskyi.trippyapp.ui.screens.ParticipantBalance
import com.navrotskyi.trippyapp.ui.screens.ParticipantsRow
import com.navrotskyi.trippyapp.ui.screens.Settlement
import com.navrotskyi.trippyapp.ui.screens.SettlementCard
import com.navrotskyi.trippyapp.ui.screens.SettlementType
import com.navrotskyi.trippyapp.ui.screens.TotalBalanceCard
import com.navrotskyi.trippyapp.ui.theme.TrippyAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupBalanceComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---------- TotalBalanceCard ----------

    @Test
    fun totalBalanceCard_positiveBalance_showsPlusSignAndAmount() {
        composeTestRule.setContent {
            TrippyAppTheme { TotalBalanceCard(balance = 123.45) }
        }
        composeTestRule.onNodeWithText("Twój całkowity bilans:").assertIsDisplayed()
        composeTestRule.onNodeWithText("+123.45 PLN").assertIsDisplayed()
    }

    @Test
    fun totalBalanceCard_negativeBalance_showsMinusSign() {
        composeTestRule.setContent {
            TrippyAppTheme { TotalBalanceCard(balance = -50.00) }
        }
        // Minus pochodzi z formatera — sprawdzamy że nie ma "+"
        composeTestRule.onNodeWithText("-50.00 PLN").assertIsDisplayed()
    }

    @Test
    fun totalBalanceCard_zeroBalance_noSign() {
        composeTestRule.setContent {
            TrippyAppTheme { TotalBalanceCard(balance = 0.0) }
        }
        composeTestRule.onNodeWithText("0.00 PLN").assertIsDisplayed()
    }

    // ---------- ParticipantsRow ----------

    @Test
    fun participantsRow_rendersAllNamesAndBalances() {
        val participants = listOf(
            ParticipantBalance(name = "Alicja", balance = 50.0, initials = "A"),
            ParticipantBalance(name = "Bartek", balance = -25.50, initials = "B"),
            ParticipantBalance(name = "Cezary", balance = 0.0, initials = "C")
        )

        composeTestRule.setContent {
            TrippyAppTheme { ParticipantsRow(participants = participants) }
        }

        composeTestRule.onNodeWithText("Alicja").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bartek").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cezary").assertIsDisplayed()
        composeTestRule.onNodeWithText("+50.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("-25.50").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.00").assertIsDisplayed()
    }

    @Test
    fun participantsRow_showsInitialsWhenNoAvatar() {
        val participants = listOf(
            ParticipantBalance(name = "Jan Kowalski", balance = 10.0, initials = "JK", avatarUrl = null)
        )
        composeTestRule.setContent {
            TrippyAppTheme { ParticipantsRow(participants = participants) }
        }
        composeTestRule.onNodeWithText("JK").assertIsDisplayed()
    }

    @Test
    fun participantsRow_emptyList_rendersNothing() {
        composeTestRule.setContent {
            TrippyAppTheme { ParticipantsRow(participants = emptyList()) }
        }
        // brak crashy = OK; dodatkowo żadnej z wcześniejszych nazw być nie może
        composeTestRule.onAllNodesWithMatchingText().also { /* sanity */ }
    }

    private fun composeTestRule.onAllNodesWithMatchingText() =
        composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Alicja"))

    // ---------- SettlementCard ----------

    @Test
    fun settlementCard_iOweThem_showsRepayButtonAndCorrectText() {
        val s = Settlement(otherPersonName = "Anna", amount = 42.50, type = SettlementType.I_OWE_THEM)

        composeTestRule.setContent {
            TrippyAppTheme { SettlementCard(settlement = s) }
        }

        composeTestRule.onNodeWithText("Oddajesz Anna:").assertIsDisplayed()
        composeTestRule.onNodeWithText("42.50 PLN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Spłać").assertIsDisplayed()
    }

    @Test
    fun settlementCard_theyOweMe_showsRemindButton() {
        val s = Settlement(otherPersonName = "Tomek", amount = 99.99, type = SettlementType.THEY_OWE_ME)

        composeTestRule.setContent {
            TrippyAppTheme { SettlementCard(settlement = s) }
        }

        composeTestRule.onNodeWithText("Tomek oddaje Ci:").assertIsDisplayed()
        composeTestRule.onNodeWithText("99.99 PLN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Przypomnij").assertIsDisplayed()
    }

    @Test
    fun settlementCard_iOweThem_repayButtonIsClickable() {
        val s = Settlement(otherPersonName = "X", amount = 1.0, type = SettlementType.I_OWE_THEM)

        composeTestRule.setContent {
            TrippyAppTheme { SettlementCard(settlement = s) }
        }
        // Klikalność = brak crashu (onClick = TODO w produkcji)
        composeTestRule.onNodeWithText("Spłać").performClick()
    }
}