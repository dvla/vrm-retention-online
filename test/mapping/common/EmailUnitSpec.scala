package mapping.common

import helpers.UnitSpec
import viewmodels.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import services.fakes.VehicleAndKeeperLookupWebServiceConstants._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import services.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber
import mappings.common.Email
import play.api.data.validation.{ValidationError, Invalid, Valid}

final class EmailUnitSpec extends UnitSpec {

  "email validation" should {

    //
    // success scenarios
    //

    "find the test@io email address valid" in {

      val emailAddress = "test@io"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@iana.org email address valid" in {

      val emailAddress = "test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@nominet.org.uk email address valid" in {

      val emailAddress = "test@nominet.org.uk"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@about.museum email address valid" in {

      val emailAddress = "ttest@about.museum"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the a@iana.org email address valid" in {

      val emailAddress = "a@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@e.com email address valid" in {

      val emailAddress = "test@e.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@iana.a email address valid" in {

      val emailAddress = "test@iana.a"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test.test@iana.org email address valid" in {

      val emailAddress = "test.test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the !#$%&`*+/=?^`{|}~@iana.org email address valid" in {

      val emailAddress = "!#$%&`*+/=?^`{|}~@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the 123@iana.org email address valid" in {

      val emailAddress = "123@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@123.com email address valid" in {

      val emailAddress = "test@123.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@iana.123 email address valid" in {

      val emailAddress = "test@iana.123"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@iana.org email address valid" in {

      val emailAddress = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.com email address valid" in {

      val emailAddress = "test@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@mason-dixon.com email address valid" in {

      val emailAddress = "test@mason-dixon.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@c--n.com email address valid" in {

      val emailAddress = "test@c--n.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the test@iana.co-uk email address valid" in {

      val emailAddress = "test@iana.co-uk"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the a@a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v email address valid" in {

      val emailAddress = "a@a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghi email address valid" in {

      val emailAddress = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghi"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the \"test\"@iana.orgemail address valid" in {

      val emailAddress = "\"test\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the \"\\a\"@iana.org address valid" in {

      val emailAddress = "\"\\a\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the \"\\\"\"@iana.org address valid" in {

      val emailAddress = "\"\\\"\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    "find the xn--test@iana.org address valid" in {

      val emailAddress = "xn--test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Valid)
    }

    //
    // failure scenarios
    //
    "find the test email address invalid" in {

      val emailAddress = "test"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the @ email address invalid" in {

      val emailAddress = "@"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@ email address invalid" in {

      val emailAddress = "test@"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the @io email address invalid" in {

      val emailAddress = "@io"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the @iana.org email address invalid" in {

      val emailAddress = "@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the .test@iana.org email address invalid" in {

      val emailAddress = ".test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test.@iana.org email address invalid" in {

      val emailAddress = "test.@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test..iana.org email address invalid" in {

      val emailAddress = "test..iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test_exa-mple.com email address invalid" in {

      val emailAddress = "test_exa-mple.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test\\@test@iana.org email address invalid" in {

      val emailAddress = "test\\@test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklmn@iana.org email address invalid" in {

      val emailAddress = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklmn@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm.com email address invalid" in {

      val emailAddress = "test@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@-iana.org email address invalid" in {

      val emailAddress = "test@-iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@iana-.com email address invalid" in {

      val emailAddress = "test@iana-.com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@.iana.org email address invalid" in {

      val emailAddress = "test@.iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@iana.org. email address invalid" in {

      val emailAddress = "test@iana.org."

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@iana..com email address invalid" in {

      val emailAddress = "test@iana..com"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij email address invalid" in {

      val emailAddress = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the a@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefg.hij email address invalid" in {

      val emailAddress = "a@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefg.hij"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the a@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefg.hijk email address invalid" in {

      val emailAddress = "a@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefg.hijk"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"\"\"@iana.org email address invalid" in {

      val emailAddress = "\"\"\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test\"@iana.org email address invalid" in {

      val emailAddress = "test\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"test@iana.org email address invalid" in {

      val emailAddress = "\"test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"test\"test@iana.org email address invalid" in {

      val emailAddress = "\"test\"test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test\"text\"@iana.org email address invalid" in {

      val emailAddress = "test\"text\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"test\"\"test\"@iana.org email address invalid" in {

      val emailAddress = "\"test\"\"test\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"test\".\"test\"@iana.org email address invalid" in {

      val emailAddress = "\"test\".\"test\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"test\".test@iana.org email address invalid" in {

      val emailAddress = "\"test\".test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@iana.org- email address invalid" in {

      val emailAddress = "test@iana.org-"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the (test@iana.org email address invalid" in {

      val emailAddress = "(test@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@(iana.org email address invalid" in {

      val emailAddress = "test@(iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the \"test\\\"@iana.org email address invalid" in {

      val emailAddress = "\"test\\\"@iana.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@.org email address invalid" in {

      val emailAddress = "test@.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }

    "find the test@iana/icann.org email address invalid" in {

      val emailAddress = "test@iana/icann.org"

      val result = Email.emailAddress(emailAddress)

      result should equal(Invalid(ValidationError("error.email")))
    }
  }
}