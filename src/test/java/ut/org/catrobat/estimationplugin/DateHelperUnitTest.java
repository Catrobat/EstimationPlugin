package ut.org.catrobat.estimationplugin;

import static org.junit.Assert.*;

import org.catrobat.estimationplugin.helper.DateHelper;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DateHelperUnitTest {

    @Test
    public void testGetStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 0, 31, 23, 59, 59);
        Date input = calendar.getTime();
        Date output = DateHelper.getStartOfMonth(input);
        calendar.set(2000, 0, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date expected = calendar.getTime();
        assertEquals(expected, output);
    }

    @Test
    public void testGetEndOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 0, 1, 0, 0, 0);
        Date input = calendar.getTime();
        Date output = DateHelper.getEndOfMonth(input);
        calendar.set(2000, 0, 31, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        Date expected = calendar.getTime();
        assertEquals(expected, output);
    }

    @Test
    public void testGetStartOfNextMonth() {
    }

    @Test
    public void getEndOfNextMonth() {
    }
}
