final class AdditionalInfoStackView: UIView {

  private let stackView = UIStackView()

  init() {
    super.init(frame: .zero)
    setupViews()
    arrangeViews()
    layoutViews()
  }

  required init?(coder: NSCoder) {
    super.init(coder: coder)
    setupViews()
    arrangeViews()
    layoutViews()
  }

  // MARK: - Private
  private func setupViews() {
    stackView.axis = .vertical
    stackView.distribution = .equalSpacing
    stackView.spacing = 16
  }

  private func arrangeViews() {
    addSubview(stackView)
  }

  private func layoutViews() {
    stackView.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      stackView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20),
      stackView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20),
      stackView.topAnchor.constraint(equalTo: topAnchor),
      stackView.bottomAnchor.constraint(equalTo: bottomAnchor)
    ])
  }

  func addItem(image: UIImage, text: String) {
    let infoSectionStackView = UIStackView()
    infoSectionStackView.axis = .horizontal
    infoSectionStackView.distribution = .fillProportionally
    infoSectionStackView.alignment = .center
    infoSectionStackView.spacing = 16

    infoSectionStackView.addArrangedSubview(Self.imageView(for: image))
    infoSectionStackView.addArrangedSubview(Self.descriptionLabel(for: text))

    stackView.addArrangedSubview(infoSectionStackView)
  }

  private static func descriptionLabel(for text: String) -> UIView {
    let descriptionLabel = UILabel()
    descriptionLabel.styleName = "regular14:blackPrimaryText"
    descriptionLabel.lineBreakMode = .byWordWrapping
    descriptionLabel.numberOfLines = .zero
    descriptionLabel.text = text.replacingOccurrences(of: "•", with: "").trimmingCharacters(in: .whitespacesAndNewlines)
    return descriptionLabel
  }

  private static func imageView(for image: UIImage) -> UIView {
    let imageView = UIImageView(image: image)
    imageView.contentMode = .scaleAspectFit
    imageView.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      imageView.heightAnchor.constraint(equalToConstant: 24),
      imageView.widthAnchor.constraint(equalTo: imageView.heightAnchor)
    ])
    return imageView
  }
}

