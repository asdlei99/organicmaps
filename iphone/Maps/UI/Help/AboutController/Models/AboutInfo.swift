enum AboutInfo: CaseIterable {
  case faq
  case reportABug
  case reportMapDataProblem
  case volunteer
  case news
  case rateTheApp

  var title: String {
    switch self {
    case .faq:
      return L("faq")
    case .reportABug:
      return L("report_a_bug")
    case .reportMapDataProblem:
      return L("report_incorrect_map_bug")
    case .volunteer:
      return L("volunteer")
    case .news:
      return L("news")
    case .rateTheApp:
      return L("rate_the_app")
    }
  }

  var image: UIImage {
    switch self {
    case .faq:
      return UIImage(named: "ic_about_faq")!
    case .reportABug:
      return UIImage(named: "ic_about_report_bug")!
    case .reportMapDataProblem:
      return UIImage(named: "ic_about_report_osm")!
    case .volunteer:
      return UIImage(named: "ic_about_volunteer")!
    case .news:
      return UIImage(named: "ic_about_news")!
    case .rateTheApp:
      return UIImage(named: "ic_about_rate_app")!
    }
  }

  var link: String? {
    switch self {
    case .faq:
      return nil
    case .reportABug:
      return "ios@organicmaps.app"
    case .reportMapDataProblem:
      return "https://organicmaps.app/faq"
    case .volunteer:
      return "https://organicmaps.app/volunteering"
    case .news:
      return L("translated_om_site_url") + "news/"
    case .rateTheApp:
      return nil
    }
  }
}
