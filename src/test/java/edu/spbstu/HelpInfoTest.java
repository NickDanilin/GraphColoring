package edu.spbstu;

import edu.spbstu.menu.HelpInfoProvider;
import edu.spbstu.menu.ResourceClassHelpInfoProvider;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HelpInfoTest {
    @Test
    void shouldGiveHelpInfoFromResource() {
        //arrange
        HelpInfoProvider helpInfoProvider = new ResourceClassHelpInfoProvider(
                HelpInfoTest.class,
                "/testfile.txt"
        );

        //act
        String helpInfo = helpInfoProvider.getHelpInfo();

        //assert
        String expected = "some text\r\nanother\r\n";

        assertThat(helpInfo, equalTo(expected));
    }
}
