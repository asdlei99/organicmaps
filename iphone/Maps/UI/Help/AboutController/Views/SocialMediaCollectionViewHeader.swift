final class SocialMediaCollectionViewHeader: UICollectionReusableView {

  static let reuseIdentifier = String(describing: self)

  private let titleLabel = UILabel()

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupView()
  }

  @available(*, unavailable)
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func setupView() {
    addSubview(titleLabel)
    titleLabel.setStyleAndApply("regular16:blackPrimaryText")
    titleLabel.numberOfLines = 1
    titleLabel.allowsDefaultTighteningForTruncation = true
    titleLabel.adjustsFontSizeToFitWidth = true
    titleLabel.minimumScaleFactor = 0.5
  }

  func setTitle(_ title: String) {
    titleLabel.text = title
  }
}
